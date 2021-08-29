package pcl.lc.irc.hooks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaState.Library;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

public class JNLuaSandbox extends AbstractListener {
    private static LuaState luaState;

    public String chan;
    public String target = null;
    private StringBuilder output;

    private String luasb;
    private String sparser;
    private String selene;

    public JNLuaSandbox() throws IOException {
        super();
        InputStream luain = getClass().getResourceAsStream("/jnlua/luasb.lua");
        luasb = CharStreams.toString(new InputStreamReader(luain, Charsets.UTF_8));

        InputStream sparserin = getClass().getResourceAsStream("/jnlua/selene/parser.lua");
        sparser = CharStreams.toString(new InputStreamReader(sparserin, Charsets.UTF_8));

        InputStream selenein = getClass().getResourceAsStream("/jnlua/selene/init.lua");
        selene = CharStreams.toString(new InputStreamReader(selenein, Charsets.UTF_8));

        initLua();
        IRCBot.registerCommand("lua", "Lua sandbox");
        IRCBot.registerCommand("selene", "Runs selene code %blame Vexatos");
        IRCBot.registerCommand("sel", "Runs selene code %blame Vexatos");
        IRCBot.registerCommand("resetlua", "Resets the lua sandbox");
    }

    @Override
    protected void initHook() {

    }

    private static String stackToString(LuaState luaState) {
        int top = luaState.getTop();
        if (top > 0) {
            StringBuilder results = new StringBuilder();
            for (int i = 1; i <= top; i++) {
                String result;
                if (luaState.isString(i) || luaState.isNumber(i))
                    result = luaState.toString(i);
                else if (luaState.isBoolean(i))
                    result = luaState.toBoolean(i) ? "true" : "false";
                else if (luaState.isNil(i))
                    result = luaState.typeName(i);
                else
                    result = String.format("%s: 0x%x", luaState.typeName(i), luaState.toPointer(i));
                results.append(result);
                if (i < top)
                    results.append(", ");
            }
            return results.toString();
        } else {
            return "";
        }
    }

    protected void initLua() {
        if (luaState != null) {
            luaState.close();
        }
        luaState = new LuaState(1024 * 1024);
        luaState.openLib(Library.BASE);
        luaState.openLib(Library.COROUTINE);
        luaState.openLib(Library.TABLE);
        luaState.openLib(Library.STRING);
        luaState.openLib(Library.BIT32);
        luaState.openLib(Library.MATH);
        luaState.openLib(Library.OS);
        luaState.openLib(Library.PACKAGE);
        luaState.openLib(Library.DEBUG);
        luaState.setTop(0); // Remove tables from loaded libraries
        luaState.pushJavaFunction(new JavaFunction() {
            @Override
            public int invoke(LuaState luaState) {
                String results = stackToString(luaState);
                output.append(results + "\n");
                return 0;
            }
        });
        luaState.setGlobal("print");

        luaState.load(luasb, "=luasb");
        luaState.call(0, 0);

        System.out.println(runScriptInSandbox("selene = (function()\n" + selene + "\nend)()"));
        System.out.println(runScriptInSandbox("selene.parser =(function()\n" + sparser + "\nend)()"));
        System.out.println(runScriptInSandbox("selene.load()"));
    }

    static String runScriptInSandbox(String script) {
        luaState.setTop(0); // Ensure stack is clean
        luaState.getGlobal("lua");
        luaState.pushString(script);
        try {
            luaState.call(1, LuaState.MULTRET);
        } catch (LuaException error) {
            luaState.setTop(0);
            return error.getMessage();
        }
        String results = stackToString(luaState);
        luaState.setTop(0); // Remove results from stack
        return results.toString();
    }

    @Override
    public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
        if (command.equals(Config.commandprefix + "lua")) {
            target = Helper.getTarget(event);
            String message = "";
            for (String aCopyOfRange : copyOfRange) {
                message = message + " " + aCopyOfRange;
            }
            if (message.trim().startsWith("http://") || message.trim().startsWith("https://")) {
                Pattern urlPattern = Pattern.compile(
                        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                //Pattern REGEX = Pattern.compile("^(http|https):\\/\\/(www).([a-z\\.]*)?(\\/[a-z1-9\\/]*)*\\??([\\&a-z1-9=]*)?");
                Matcher matcher = urlPattern.matcher(message);
                if (matcher.find()) {
                    try {
                        URL url = null;
                        System.out.println(matcher.group());
                        url = new URL(matcher.group());

                        URLConnection con = url.openConnection();
                        InputStream in = con.getInputStream();
                        String encoding = con.getContentEncoding();  // ** WRONG: should use "con.getContentType()" instead but it returns something like "text/html; charset=UTF-8" so this value must be parsed to extract the actual encoding
                        encoding = encoding == null ? "UTF-8" : encoding;
                        String body = IOUtils.toString(in, encoding);
                        System.out.println(body);
                        output = new StringBuilder();
                        output.append(runScriptInSandbox(body));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                output = new StringBuilder();
                output.append(runScriptInSandbox(message));
            }


            // Trim last newline
            if (output.length() > 0 && output.charAt(output.length() - 1) == '\n')
                output.setLength(output.length() - 1);
            String luaOut = output.toString().replace("\n", " | ").replace("\r", "");

            if (luaOut.length() > 0) {
                Helper.AntiPings = Helper.getNamesFromTarget(target);
                Helper.sendMessage(target, luaOut);
            }

        } else if (command.equals(Config.commandprefix + "selene") || command.equals(Config.commandprefix + "sel")) {
            target = Helper.getTarget(event);
            String message = "";
            for (String aCopyOfRange : copyOfRange) {
                message = message + " " + aCopyOfRange;
            }

            output = new StringBuilder();
            output.append(runScriptInSandbox(runScriptInSandbox("selene.parse([==========[" + message + "]==========])")));
            // Trim last newline
            if (output.length() > 0 && output.charAt(output.length() - 1) == '\n')
                output.setLength(output.length() - 1);
            String luaOut = output.toString().replace("\n", " | ").replace("\r", "");

            if (luaOut.length() > 0) {
                Helper.AntiPings = Helper.getNamesFromTarget(target);
                Helper.sendMessage(target, luaOut);
            }

        } else if (command.equals(Config.commandprefix + "resetlua")) {
            target = Helper.getTarget(event);
            initLua();
            Helper.sendMessage(target, "Sandbox reset");
        }
    }
}
