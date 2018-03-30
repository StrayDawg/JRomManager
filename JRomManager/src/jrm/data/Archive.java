package jrm.data;

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;

@SuppressWarnings("serial")
public class Archive extends Container implements Serializable
{
	public Archive(File file)
	{
		super(getType(file), file);
	}

	public Archive(File file, BasicFileAttributes attr)
	{
		super(getType(file), file, attr);
	}

}