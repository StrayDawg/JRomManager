package jrm.profiler.report;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profiler.data.Entity;
import jrm.profiler.data.Entry;

public class EntryMissingDuplicate extends Note
{
	Entity entity;
	Entry entry;
	
	public EntryMissingDuplicate(Entity entity, Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryMissingDuplicate.MissingDuplicate"), parent.ware.getFullName(), entry.file, entity.getName()); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryMissingDuplicate.MissingDuplicate")), toBlue(parent.ware.getFullName()), toBold(entry.file), toBold(entity.getName()))); //$NON-NLS-1$
	}

}
