/**
 * 
 */
package pcl.lc.utils;

/*******************************************************************************
 * Copyright (c) 2004, 2005
 * Thomas Hallgren, Kenneth Olwing, Mitch Sonies
 * Pontus Rydin, Nils Unden, Peer Torngren
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the individual
 * copyright holders listed above, as Initial Contributors under such license.
 * The text of such license is available at www.eclipse.org.
 *******************************************************************************/
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class TimedHashMap<K,V> implements Map<K,V>
{
	public interface EvictionPolicy<EK,EV>
	{
		void evict(Entry<EK,EV> entry);
	}

	private static final Timer s_timer = new Timer();
	
	private final long m_keepAliveTime;
	private final EvictionPolicy<K,V> m_evictionPolicy;

	private final HashMap<K,TimedEntry> m_map = new HashMap<K,TimedEntry>();

	final class TimedEntry extends TimerTask implements Map.Entry<K,V>
	{
		private final K m_key;
		private V m_value;

		TimedEntry(K key, V value)
		{
			m_key = key;
			m_value = value;
		}

		@Override
		public void run()
		{
			TimedEntry val = null;
			synchronized(m_map)
			{
				val = m_map.remove(m_key);
			}
			if(val != null && m_evictionPolicy != null)
				m_evictionPolicy.evict(val);
		}

		public K getKey()
		{
			return m_key;
		}

		public V getValue()
		{
			return m_value;
		}

		public V setValue(V value)
		{
			V old = m_value;
			this.remove();
			m_value = value;
			s_timer.schedule(this, m_keepAliveTime);
			return old;
		}

		void remove()
		{
			this.cancel();
			if(m_evictionPolicy != null)
				m_evictionPolicy.evict(this);
		}
	}

	public TimedHashMap(long keepAliveTime, EvictionPolicy<K,V> evictionPolicy)
	{
		m_keepAliveTime = keepAliveTime;
		m_evictionPolicy = evictionPolicy;
	}

	public void cancel(K key)
	{
		TimedEntry entry = m_map.get(key);
		if(entry != null)
			entry.cancel();
	}

	public V put(K key, V value)
	{
		V oldVal;
		TimedEntry entry = new TimedEntry(key, value);
		synchronized(m_map)
		{
			TimedEntry oldEntry = m_map.get(key);

			if(oldEntry != null)
			{
				oldVal = oldEntry.getValue();
				oldEntry.remove();
			}
			else
				oldVal = null;

			m_map.put(key, entry);
		}
		if(scheduleOnPut())
			s_timer.schedule(entry, m_keepAliveTime);
		return oldVal;
	}

	public void schedule(K key)
	{
		TimedEntry entry = m_map.get(key);
		if(entry != null)
			s_timer.schedule(entry, m_keepAliveTime);
	}

	public boolean scheduleOnPut()
	{
		return true;
	}

	public int size()
	{
		return m_map.size();
	}

	public boolean isEmpty()
	{
		return m_map.isEmpty();
	}

	public boolean containsKey(Object key)
	{
		synchronized(m_map)
		{
			return m_map.containsKey(key);
		}
	}

	public boolean containsValue(Object value)
	{
		synchronized(m_map)
		{
			for(Entry<K,V> te : m_map.values())
			{
				Object tv = te.getValue();
				if(value == null)
				{
					if(tv == null)
						return true;
				}
				else if(value.equals(tv))
					return true;
			}
		}
		return false;
	}

	public V get(Object key)
	{
		synchronized(m_map)
		{
			Entry<K,V> te = m_map.get(key);
			return te == null ? null : te.getValue();
		}
	}

	public V remove(Object key)
	{
		synchronized(m_map)
		{
			TimedEntry te = m_map.remove(key);
			if(te == null)
				return null;
			V oldVal = te.getValue();
			te.remove();
			return oldVal;
		}
	}

	public void putAll(Map<? extends K, ? extends V> t)
	{
		for(Entry<? extends K,? extends V> entry : t.entrySet())
			this.put(entry.getKey(), entry.getValue());
	}

	public void clear()
	{
		synchronized(m_map)
		{
			for(TimedEntry entry : m_map.values())
				entry.remove();
			m_map.clear();
		}
	}

	public Set<K> keySet()
	{
		return m_map.keySet();
	}

	public Collection<V> values()
	{
		final Iterator<TimedEntry> entries = m_map.values().iterator();
		return new AbstractCollection<V>()
		{
			@Override
			public Iterator<V> iterator()
			{
				return new Iterator<V>()
				{

					public boolean hasNext()
					{
						return entries.hasNext();
					}

					public V next()
					{
						return entries.next().getValue();
					}

					public void remove()
					{
						entries.remove();
					}
				};
			}

			@Override
			public int size()
			{
				return m_map.size();
			}
		};
	}

	public Set<Entry<K, V>> entrySet()
	{
		final Iterator<TimedEntry> entries = m_map.values().iterator();
		return new AbstractSet<Entry<K, V>>()
		{
			@Override
			public Iterator<Entry<K, V>> iterator()
			{
				return new Iterator<Entry<K,V>>()
				{

					public boolean hasNext()
					{
						return entries.hasNext();
					}

					public Entry<K,V> next()
					{
						return entries.next();
					}

					public void remove()
					{
						entries.remove();
					}
				};
			}

			@Override
			public int size()
			{
				return m_map.size();
			}			
		};
	}
}


