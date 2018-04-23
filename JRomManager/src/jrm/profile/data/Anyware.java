package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import jrm.profile.data.Entity.Status;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.ui.AnywareRenderer;
import one.util.streamex.StreamEx;

@SuppressWarnings("serial")
public abstract class Anyware implements Serializable, Comparable<Anyware>, TableModel, Systm
{
	public String name; // required
	public String cloneof = null;
	public StringBuffer description = new StringBuffer();
	public StringBuffer year = new StringBuffer();

	public ArrayList<Rom> roms = new ArrayList<>();
	public ArrayList<Disk> disks = new ArrayList<>();

	public HashMap<String, Anyware> clones = new HashMap<>();

	public Anyware parent = null;

	public static transient MergeOptions merge_mode;
	public static transient HashCollisionOptions hash_collision_mode;
	protected transient boolean collision;

	private static transient EventListenerList listenerList;
	private static transient EnumSet<EntityStatus> filter = null;
	private transient List<Entity> table_entities;

	public Anyware()
	{
		initTransient();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransient();
	}

	private void initTransient()
	{
		if(merge_mode == null)
			merge_mode = MergeOptions.SPLIT;
		if(hash_collision_mode == null)
			hash_collision_mode = HashCollisionOptions.SINGLEFILE;
		collision = false;
		table_entities = null;
		if(listenerList == null)
			listenerList = new EventListenerList();
		if(filter == null)
			filter = EnumSet.allOf(EntityStatus.class);
	}

	public <T extends Anyware> T getParent(Class<T> type)
	{
		return type.cast(parent);
	}

	public abstract Anyware getParent();

	public abstract String getName();

	public void setName(String name)
	{
		this.name = name;
	}

	public abstract String getFullName();

	public abstract String getFullName(String filename);

	public boolean isCollisionMode()
	{
		return collision;
	}

	public void setCollisionMode(boolean parent)
	{
		if(parent)
			getDest(merge_mode).clones.forEach((n, m) -> m.collision = true);
		this.collision = true;
	}

	public void resetCollisionMode()
	{
		collision = false;
		roms.forEach(Rom::resetCollisionMode);
		disks.forEach(Disk::resetCollisionMode);
	}

	public boolean isClone()
	{
		return (parent != null && !getParent().isBios());
	}

	public Anyware getDest(MergeOptions merge_mode)
	{
		Machine.merge_mode = merge_mode;
		if(merge_mode.isMerge() && isClone())
			return getParent().getDest(merge_mode);
		return this;
	}

	public abstract boolean isBios();

	public abstract boolean isRomOf();

	public List<Disk> filterDisks(MergeOptions merge_mode, HashCollisionOptions hash_collision_mode)
	{
		Machine.merge_mode = merge_mode;
		Machine.hash_collision_mode = hash_collision_mode;
		Stream<Disk> stream;
		if(merge_mode.isMerge())
		{
			if(isClone())
				stream = Stream.empty();
			else
			{
				List<Disk> disks_with_clones = Stream.concat(disks.stream(), clones.values().stream().flatMap(m -> m.disks.stream())).collect(Collectors.toList());
				StreamEx.of(disks_with_clones).groupingBy(Disk::getName).forEach((n, l) -> {
					if(l.size() > 1 && StreamEx.of(l).distinct(Disk::hashString).count() > 1)
						l.forEach(Disk::setCollisionMode);
				});
				stream = StreamEx.of(disks_with_clones).distinct(Disk::getName);
			}
		}
		else
			stream = disks.stream();
		return stream.filter(d -> {
			if(d.status == Status.nodump)
				return false;
			if(merge_mode == MergeOptions.SPLIT && d.merge != null)
				return false;
			if(merge_mode == MergeOptions.NOMERGE && d.merge != null)
				return true;
			return this.isBios() || !this.isRomOf() || d.merge == null;
		}).collect(Collectors.toList());
	}

	public List<Rom> filterRoms(MergeOptions merge_mode, HashCollisionOptions hash_collision_mode)
	{
		Machine.merge_mode = merge_mode;
		Machine.hash_collision_mode = hash_collision_mode;
		Stream<Rom> stream;
		if(merge_mode.isMerge())
		{
			if(isClone())
				stream = Stream.empty();
			else
			{
				List<Rom> roms_with_clones = Stream.concat(roms.stream(), clones.values().stream().flatMap(m -> m.roms.stream())).collect(Collectors.toList());
				StreamEx.of(roms_with_clones).groupingBy(Rom::getName).forEach((n, l) -> {
					if(l.size() > 1 && StreamEx.of(l).distinct(Rom::hashString).count() > 1)
						l.forEach(Rom::setCollisionMode);
				});
				stream = StreamEx.of(roms_with_clones).distinct(Rom::getName);
			}
		}
		else
			stream = roms.stream();
		return stream.filter(r -> {
			if(r.status == Status.nodump)
				return false;
			if(r.crc == null)
				return false;
			if(merge_mode == MergeOptions.SPLIT && r.merge != null)
				return false;
			if(merge_mode == MergeOptions.NOMERGE && r.bios != null)
				return false;
			if(merge_mode == MergeOptions.NOMERGE && r.merge != null)
				return true;
			if(merge_mode == MergeOptions.FULLNOMERGE && r.bios != null)
				return true;
			if(merge_mode == MergeOptions.FULLNOMERGE && r.merge != null)
				return true;
			if(merge_mode == MergeOptions.MERGE && r.bios != null)
				return false;
			if(merge_mode == MergeOptions.MERGE && r.merge != null)
				return true;
			if(merge_mode == MergeOptions.FULLMERGE && r.bios != null)
				return true;
			if(merge_mode == MergeOptions.FULLMERGE && r.merge != null)
				return true;
			return this.isBios() || !this.isRomOf() || r.merge == null;
		}).collect(Collectors.toList());
	}

	public void reset()
	{
		this.table_entities = null;
		fireTableChanged(new TableModelEvent(this));
	}
	
	public void setFilter(EnumSet<EntityStatus> filter)
	{
		Anyware.filter = filter;
		reset();
	}

	private List<Entity> getEntities()
	{
		if(table_entities == null)
			table_entities = Stream.concat(roms.stream(), disks.stream()).filter(t -> filter.contains(t.getStatus())).sorted().collect(Collectors.toList());
		return table_entities;
	}

	@Override
	public int getRowCount()
	{
		return getEntities().size();
	}

	@Override
	public int getColumnCount()
	{
		return AnywareRenderer.columns.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return AnywareRenderer.columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return AnywareRenderer.columnsTypes[columnIndex];
	}

	public static TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return AnywareRenderer.columnsRenderers[columnIndex] != null ? AnywareRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	public static int getColumnWidth(int columnIndex)
	{
		return AnywareRenderer.columnsWidths[columnIndex];
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
			case 0:
				return getEntities().get(rowIndex);
			case 1:
				return getEntities().get(rowIndex);
			case 2:
				return getEntities().get(rowIndex).size;
			case 3:
				return getEntities().get(rowIndex).crc;
			case 4:
				return getEntities().get(rowIndex).md5;
			case 5:
				return getEntities().get(rowIndex).sha1;
		}
		return null;
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

	@Override
	public int compareTo(Anyware o)
	{
		return this.name.compareTo(o.name);
	}

	public AnywareStatus getStatus()
	{
		AnywareStatus status = AnywareStatus.COMPLETE;
		boolean ok = false;
		for(Disk disk : disks)
		{
			EntityStatus estatus = disk.getStatus();
			if(estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if(estatus == EntityStatus.OK)
				ok = true;
			else if(estatus == EntityStatus.UNKNOWN)
			{
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		for(Rom rom : roms)
		{
			EntityStatus estatus = rom.getStatus();
			if(estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if(estatus == EntityStatus.OK)
				ok = true;
			else if(estatus == EntityStatus.UNKNOWN)
			{
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		if(status == AnywareStatus.PARTIAL && !ok)
			status = AnywareStatus.MISSING;
		return status;
	}

	public int countHave()
	{
		int have = 0;
		for(Disk disk : disks)
			if(disk.getStatus() == EntityStatus.OK)
				have++;
		for(Rom rom : roms)
			if(rom.getStatus() == EntityStatus.OK)
				have++;
		return have;
	}

	public int countAll()
	{
		return roms.size() + disks.size();
	}
	

}