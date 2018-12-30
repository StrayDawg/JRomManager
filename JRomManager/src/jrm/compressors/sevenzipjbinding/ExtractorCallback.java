package jrm.compressors.sevenzipjbinding;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import jrm.misc.Log;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;

public abstract class ExtractorCallback implements IArchiveExtractCallback
{
	/**
	 * 
	 */
	private final NArchiveBase nArchive;
	private final File baseDir;
	private final HashMap<Integer, File> tmpfiles;
	private final HashMap<Integer, RandomAccessFile> rafs;
	private boolean skipExtraction;
	private int index;

	public ExtractorCallback(NArchiveBase nArchive, File baseDir, HashMap<Integer, File> tmpfiles, HashMap<Integer, RandomAccessFile> rafs)
	{
		this.nArchive = nArchive;
		this.baseDir = baseDir;
		this.tmpfiles = tmpfiles;
		this.rafs = rafs;
	}

	@Override
	public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException
	{
	}

	@Override
	public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException
	{
		if (skipExtraction)
			return;
		if (extractOperationResult != ExtractOperationResult.OK)
			System.err.println("Extraction error");
		else
		{
			try
			{
				rafs.get(index).close();
				String path  = (String) this.nArchive.getIInArchive().getProperty(index, PropID.PATH);
				File tmpfile = tmpfiles.get(index);
				File dstfile = new File(baseDir,path);
				FileUtils.forceMkdirParent(dstfile);
				if(!dstfile.exists())
					FileUtils.moveFile(tmpfile, dstfile);
			}
			catch (IOException e)
			{
				Log.err(e.getMessage(),e);
			}
		}
	}

	@Override
	public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException
	{
		this.index = index;
		skipExtraction = (Boolean) this.nArchive.getIInArchive().getProperty(index, PropID.IS_FOLDER);
		if (skipExtraction || extractAskMode != ExtractAskMode.EXTRACT)
			return null;
		return new ISequentialOutStream()
		{
			@Override
			public int write(byte[] data) throws SevenZipException
			{
				try
				{
					rafs.get(index).write(data);
					return data.length;
				}
				catch (IOException e)
				{
					Log.err(e.getMessage(),e);
				}
				return 0;
			}
		};
	}
}