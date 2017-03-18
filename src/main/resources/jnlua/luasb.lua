local env={
	_VERSION=_VERSION,
	assert=assert,
	error=error,
	ipairs=ipairs,
	next=next,
	pairs=pairs,
	pcall=pcall,
	print=print,
	rawequal=rawequal,
	rawget=rawget,
	rawlen=rawlen,
	rawset=rawset,
	select=select,
	tonumber=tonumber,
	tostring=tostring,
	type=type,
	xpcall=xpcall,
	debug={},
	io={},
	os={
		clock=os.clock,
		date=os.date,
		difftime=os.difftime,
		time=os.time,
	},
}
for _, name in pairs({"bit32", "coroutine", "math", "string", "table", "utf8"}) do
	if _ENV[name] then
		env[name]={}
		for k, v in pairs(_ENV[name]) do
			env[name][k]=v
		end
	end
end
env.string.dump=nil

local function ctype(n, ...)
	if select("#", ...) >= n then
		return type((select(n, ...)))
	else
		return "no value"
	end
end

env.load=function(...)
	local code, name, mode, fenv=...
	local top=select("#", ...)
	if type(code) ~= "string" and type(code) ~= "number" and type(code) ~= "function" then
		error("bad argument #1 to 'load' (function expected, got "..ctype(1, ...)..")", 2)
	end
	if type(mode) ~= "nil" and type(mode) ~= "string" and type(mode) ~= "number" then
		error("bad argument #3 to 'load' (string expected, got "..ctype(3, ...)..")", 2)
	end
	if type(name) ~= "nil" and type(name) ~= "string" and type(name) ~= "number" then
		error("bad argument #2 to 'load' (string expected, got "..ctype(2, ...)..")", 2)
	end
	if top < 4 then
		fenv = env
	end
	return load(code, name, "t", fenv)
end

env.getmetatable=function(...)
	local thing=...
	local top=select("#", ...)
	if top < 1 then
		error("bad argument #1 to 'getmetatable' (value expected)", 2)
	elseif type(thing) == "table" then
		return getmetatable(thing)
	else
		return nil
	end
end

env.setmetatable=function(...)
	local t, mt=...
	local top=select("#", ...)
	if type(t) ~= "table" then
		error("bad argument #1 to 'setmetatable' (table expected, got "..ctype(1, ...)..")", 2)
	end
	if type(mt) ~= "table" and (type(mt) ~= "nil" or top < 2) then
		error("bad argument #2 to 'setmetatable' (nil or table expected)", 2)
	end
	if type(mt) ~= "table" or rawget(mt, "__gc") == nil then
		return setmetatable(t, mt)
	else
		local gc=rawget(mt, "__gc")
		rawset(mt, "__gc", nil)
		setmetatable(t, mt)
		rawset(mt, "__gc", gc)
		return t
	end
end

function lua(code)
	local fn, err=load("return "..code, "=main", "t", env)
	if not fn then
		fn, err=load(code, "=main", "t", env)
		if not fn then
			return err
		end
	end
	local cofn=coroutine.create(fn)
	debug.sethook(cofn, function()
		debug.sethook(cofn)
		debug.sethook(cofn, function()
			error("script took too long", 0)
		end, "", 1)
		error("script took too long", 0)
	end, "", 20000)
	return select(2, coroutine.resume(cofn))
end

lua("function uhm(a, r)local b="" for i=1,#a,(r or 0.5)do local c=i+(math.random()>=(i%1)and 1 or 0)b=b..a:sub(c, c)end return b end")