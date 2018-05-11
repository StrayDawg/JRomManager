package jrm.profile.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.tree.TreeNode;

import org.apache.commons.lang3.StringUtils;

import jrm.ui.AbstractNGTreeNode;

public class CatVer extends AbstractNGTreeNode
{
	private Map<String, Category> categories = new TreeMap<>();
	private List<Category> list_categories = null;

	class Category extends AbstractNGTreeNode implements Map<String, SubCategory>
	{
		String name;
		private CatVer parent = null;
		private Map<String, SubCategory> subcategories = new TreeMap<>();
		private List<SubCategory> list_subcategories = null;

		public Category(String name)
		{
			this.name = name;
			this.parent = CatVer.this;
		}

		@Override
		public int size()
		{
			return subcategories.size();
		}

		@Override
		public boolean isEmpty()
		{
			return subcategories.isEmpty();
		}

		@Override
		public boolean containsKey(Object key)
		{
			return subcategories.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value)
		{
			return subcategories.containsValue(value);
		}

		@Override
		public SubCategory get(Object key)
		{
			return subcategories.get(key);
		}

		@Override
		public SubCategory put(String key, SubCategory value)
		{
			return subcategories.put(key, value);
		}

		@Override
		public SubCategory remove(Object key)
		{
			return subcategories.remove(key);
		}

		@Override
		public void putAll(Map<? extends String, ? extends SubCategory> m)
		{
			subcategories.putAll(m);
		}

		@Override
		public void clear()
		{
			subcategories.clear();
		}

		@Override
		public Set<String> keySet()
		{
			return subcategories.keySet();
		}

		@Override
		public Collection<SubCategory> values()
		{
			return subcategories.values();
		}

		@Override
		public Set<Entry<String, SubCategory>> entrySet()
		{
			return subcategories.entrySet();
		}

		@Override
		public TreeNode getChildAt(int childIndex)
		{
			return list_subcategories.get(childIndex);
		}

		@Override
		public int getChildCount()
		{
			return list_subcategories.size();
		}

		@Override
		public TreeNode getParent()
		{
			return parent;
		}

		@Override
		public int getIndex(TreeNode node)
		{
			return list_subcategories.indexOf(node);
		}

		@Override
		public boolean getAllowsChildren()
		{
			return true;
		}

		@Override
		public boolean isLeaf()
		{
			return list_subcategories.size()==0;
		}

		@Override
		public Enumeration<SubCategory> children()
		{
			return Collections.enumeration(list_subcategories);
		}

		@Override
		public Object getUserObject()
		{
			return String.format("%s (%d)", name, list_subcategories.stream().filter(SubCategory::isSelected).mapToInt(SubCategory::size).sum());
		}
	}

	class SubCategory extends AbstractNGTreeNode implements List<String>, TreeNode
	{
		String name;
		Category parent;
		private List<String> games = new ArrayList<>();

		public SubCategory(String name)
		{
			this.name = name;
		}

		@Override
		public Iterator<String> iterator()
		{
			return games.iterator();
		}

		@Override
		public int size()
		{
			return games.size();
		}

		@Override
		public boolean isEmpty()
		{
			return games.isEmpty();
		}

		@Override
		public boolean contains(Object o)
		{
			return games.contains(o);
		}

		@Override
		public Object[] toArray()
		{
			return games.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a)
		{
			return games.toArray(a);
		}

		@Override
		public boolean add(String e)
		{
			return games.add(e);
		}

		@Override
		public boolean remove(Object o)
		{
			return games.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c)
		{
			return games.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends String> c)
		{
			return games.addAll(c);
		}

		@Override
		public boolean addAll(int index, Collection<? extends String> c)
		{
			return games.addAll(index, c);
		}

		@Override
		public boolean removeAll(Collection<?> c)
		{
			return games.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c)
		{
			return games.retainAll(c);
		}

		@Override
		public void clear()
		{
			games.clear();
		}

		@Override
		public String get(int index)
		{
			return games.get(index);
		}

		@Override
		public String set(int index, String element)
		{
			return games.set(index, element);
		}

		@Override
		public void add(int index, String element)
		{
			games.add(index, element);
		}

		@Override
		public String remove(int index)
		{
			return games.remove(index);
		}

		@Override
		public int indexOf(Object o)
		{
			return games.indexOf(o);
		}

		@Override
		public int lastIndexOf(Object o)
		{
			return games.lastIndexOf(o);
		}

		@Override
		public ListIterator<String> listIterator()
		{
			return games.listIterator();
		}

		@Override
		public ListIterator<String> listIterator(int index)
		{
			return games.listIterator(index);
		}

		@Override
		public List<String> subList(int fromIndex, int toIndex)
		{
			return games.subList(fromIndex, toIndex);
		}

		@Override
		public TreeNode getChildAt(int childIndex)
		{
			return null;
		}

		@Override
		public int getChildCount()
		{
			return 0;
		}

		@Override
		public TreeNode getParent()
		{
			return parent;
		}

		@Override
		public int getIndex(TreeNode node)
		{
			return 0;
		}

		@Override
		public boolean getAllowsChildren()
		{
			return false;
		}

		@Override
		public boolean isLeaf()
		{
			return true;
		}

		@Override
		public Enumeration<?> children()
		{
			return null;
		}

		@Override
		public Object getUserObject()
		{
			return name + " ("+games.size()+")";
		}
	}

	private CatVer(File file)
	{
		try(BufferedReader reader = new BufferedReader(new FileReader(file));)
		{
			String line;
			boolean in_section = false;
			while(null != (line = reader.readLine()))
			{
				if(line.equalsIgnoreCase("[Category]"))
					in_section = true;
				else if(line.startsWith("[") && in_section)
					break;
				else if(in_section)
				{
					String[] kv = StringUtils.split(line, '=');
					if(kv.length == 2)
					{
						String k = kv[0].trim();
						String[] v = StringUtils.split(kv[1], '/');
						if(v.length == 2)
						{
							String c = v[0].trim();
							String sc = v[1].trim();
							Category cat;
							if(!categories.containsKey(c))
								categories.put(c, cat = new Category(c));
							else
								cat = categories.get(c);
							SubCategory subcat;
							if(!cat.containsKey(sc))
								cat.put(sc, subcat = new SubCategory(sc));
							else
								subcat = cat.get(sc);
							subcat.add(k);
							subcat.parent = cat;
						}
					}
				}
			}
			list_categories = new ArrayList<>(categories.values());
			for(Category cat : list_categories)
				cat.list_subcategories = new ArrayList<>(cat.subcategories.values());
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static CatVer read(File file)
	{
		return new CatVer(file);
	}

	@Override
	public TreeNode getChildAt(int childIndex)
	{
		return list_categories.get(childIndex);
	}

	@Override
	public int getChildCount()
	{
		return list_categories.size();
	}

	@Override
	public TreeNode getParent()
	{
		return null;
	}

	@Override
	public int getIndex(TreeNode node)
	{
		return list_categories.indexOf(node);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public boolean isLeaf()
	{
		return list_categories.size()==0;
	}

	@Override
	public Enumeration<Category> children()
	{
		return Collections.enumeration(list_categories);
	}

	@Override
	public Object getUserObject()
	{
		return String.format("%s (%d)", "All Categories", list_categories.stream().flatMap(c->c.list_subcategories.stream().filter(SubCategory::isSelected)).mapToInt(SubCategory::size).sum());
	}

}