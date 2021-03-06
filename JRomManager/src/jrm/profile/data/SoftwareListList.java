/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;

import jrm.profile.Profile;
import jrm.profile.manager.Export;
import jrm.ui.progress.ProgressHandler;
import jrm.xml.EnhancedXMLStreamWriter;

/**
 * List of {@link SoftwareList}
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class SoftwareListList extends AnywareListList<SoftwareList> implements Serializable, ByName<SoftwareList>
{
	/**
	 * The {@link List} of {@link SoftwareList}
	 */
	private final ArrayList<SoftwareList> sl_list = new ArrayList<>();
	
	/**
	 * The by name {@link HashMap} of {@link SoftwareList}
	 */
	private final HashMap<String, SoftwareList> sl_byname = new HashMap<>();

	/**
	 * The constructor, will initialize transients fields
	 */
	public SoftwareListList(Profile profile)
	{
		super(profile);
		initTransient();
	}

	/**
	 * the Serializable method for special serialization handling (in that case : initialize transient default values) 
	 * @param in the serialization inputstream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransient();
	}

	@Override
	protected void initTransient()
	{
		super.initTransient();
	}


	@Override
	public void resetCache()
	{
		this.filtered_list = null;
	}

	@Override
	public void setFilterCache(final EnumSet<AnywareStatus> filter)
	{
	}

	@Override
	public List<SoftwareList> getList()
	{
		return sl_list;
	}

	@Override
	public Stream<SoftwareList> getFilteredStream()
	{
		return getList().stream().filter(sl -> {
			if(!sl.getSystem().isSelected(sl.profile))
				return false;
			return true;
		});
	}

	@Override
	public List<SoftwareList> getFilteredList()
	{
		if(filtered_list == null)
			filtered_list = getFilteredStream().filter(t -> profile.filter_ll.contains(t.getStatus())).sorted().collect(Collectors.toList());
		return filtered_list;
	}

	/**
	 * Export as dat
	 * @param writer the {@link EnhancedXMLStreamWriter} used to write output file
	 * @param progress the {@link ProgressHandler} to show the current progress
	 * @param filtered do we use the current machine filters of none
	 * @param selection the selected software list (null if none selected)
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void export(final EnhancedXMLStreamWriter writer, final ProgressHandler progress, final boolean filtered, final SoftwareList selection) throws XMLStreamException, IOException
	{
		final List<SoftwareList> lists = selection!=null?Collections.singletonList(selection):(filtered?getFilteredStream().collect(Collectors.toList()):getList());
		if(lists.size() > 0)
		{
			writer.writeStartDocument("UTF-8","1.0"); //$NON-NLS-1$ //$NON-NLS-2$
			if(lists.size() > 1)
			{
				writer.writeDTD("<!DOCTYPE softwarelists [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/softwarelists.dtd"), Charset.forName("UTF-8")) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				writer.writeStartElement("softwarelists"); //$NON-NLS-1$
			}
			else
				writer.writeDTD("<!DOCTYPE softwarelist [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/softwarelist.dtd"), Charset.forName("UTF-8")) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			progress.setProgress("Exporting", 0, lists.stream().flatMapToInt(sl -> IntStream.of(sl.size())).sum()); //$NON-NLS-1$
			progress.setProgress2(String.format("%d/%d", 0, lists.size()), 0, lists.size()); //$NON-NLS-1$
			for(final SoftwareList list : lists)
			{
				list.export(writer, filtered, progress);
				progress.setProgress2(String.format("%d/%d", progress.getValue2()+1, lists.size()), progress.getValue2()+1); //$NON-NLS-1$
			}
			writer.writeEndDocument();
		}
	}

	@Override
	public boolean containsName(String name)
	{
		return sl_byname.containsKey(name);
	}

	@Override
	public SoftwareList getByName(String name)
	{
		return sl_byname.get(name);
	}

	@Override
	public SoftwareList putByName(SoftwareList t)
	{
		return sl_byname.put(t.name, t);
	}

	/**
	 * named map filtered cache
	 */
	private transient Map<String, SoftwareList> sl_filtered_byname = null;

	@Override
	public void resetFilteredName()
	{
		sl_filtered_byname = getFilteredStream().collect(Collectors.toMap(SoftwareList::getBaseName, Function.identity()));
	}

	@Override
	public boolean containsFilteredName(String name)
	{
		if(sl_filtered_byname==null)
			resetFilteredName();
		return sl_filtered_byname.containsKey(name);
	}

	@Override
	public SoftwareList getFilteredByName(String name)
	{
		if(sl_filtered_byname==null)
			resetFilteredName();
		return sl_filtered_byname.get(name);
	}

	@Override
	public int count()
	{
		return getFilteredList().size();
	}

	@Override
	public SoftwareList getObject(int i)
	{
		return getFilteredList().get(i);
	}

	@Override
	public String getDescription(int i)
	{
		return getObject(i).description.toString();
	}

	@Override
	public String getHaveTot(int i)
	{
		return String.format("%d/%d", getFilteredList().get(i).countHave(), getFilteredList().get(i).countAll()); //$NON-NLS-1$
	}
}
