package jrm.profiler.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class SoftwareList implements Serializable,Comparable<SoftwareList>,TableModel
{
	private transient EventListenerList listenerList = new EventListenerList();
	private transient String[] columns = {"name","description","cloneof"};
	private transient Class<?>[] columnsTypes = {String.class,String.class,String.class};
	
	public String name;	// required
	public StringBuffer description = new StringBuffer();
	
	public List<Software> s_list = new ArrayList<>();
	public Map<String, Software> s_byname = new HashMap<>();

	public SoftwareList()
	{
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		listenerList = new EventListenerList();
		columns = new String[] {"name","description","cloneof"};
		columnsTypes = new Class<?>[] {String.class,String.class,String.class};
	}
	
	public boolean add(Software software)
	{
		software.sl = this;
		s_byname.put(software.name, software);
		return s_list.add(software);
	}

	@Override
	public int compareTo(SoftwareList o)
	{
		return this.name.compareTo(o.name);
	}

	@Override
	public int getRowCount()
	{
		return s_list.size();
	}

	@Override
	public int getColumnCount()
	{
		return columns.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return columnsTypes[columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch(columnIndex)
		{
			case 0:	return s_list.get(rowIndex).name;
			case 1:	return s_list.get(rowIndex).description.toString();
			case 2:	return s_list.get(rowIndex).cloneof;
		}
		return null;
	}
	
	public Anyware getWare(int rowIndex)
	{
		return s_list.get(rowIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
	}

	@Override
	public void addTableModelListener(TableModelListener l)
	{
		listenerList.add(TableModelListener.class, l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l)
	{
		listenerList.remove(TableModelListener.class, l);
	}

	public void fireTableChanged(TableModelEvent e)
	{
		Object[] listeners = listenerList.getListenerList();
		for(int i = listeners.length - 2; i >= 0; i -= 2)
			if(listeners[i] == TableModelListener.class)
				((TableModelListener) listeners[i + 1]).tableChanged(e);
	}
	
	public void sort()
	{
		s_list.sort(null);
	}
}
