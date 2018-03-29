package actions;

import java.util.ArrayList;
import java.util.List;

import data.Container;
import ui.ProgressHandler;

abstract public class ContainerAction
{
	public Container container;
	public ArrayList<EntryAction> entry_actions = new ArrayList<>();
	
	public ContainerAction(Container container)
	{
		this.container = container;
	}

	public void addAction(EntryAction entryAction)
	{
		entry_actions.add(entryAction);
		entryAction.parent = this;
	}
	
	public static void addToList(List<ContainerAction> list, ContainerAction action)
	{
		if (action != null && action.entry_actions.size() > 0)
			list.add(action);
	}
	
	public abstract boolean doAction(ProgressHandler handler);
}