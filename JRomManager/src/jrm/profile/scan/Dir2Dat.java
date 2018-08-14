package jrm.profile.scan;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import jrm.profile.Profile;
import jrm.profile.data.Container;
import jrm.profile.data.Entry;
import jrm.profile.data.Entry.Type;
import jrm.profile.data.Machine;
import jrm.profile.data.Software;
import jrm.profile.data.SoftwareList;
import jrm.profile.manager.Export;
import jrm.profile.manager.Export.ExportType;
import jrm.profile.scan.DirScan.Options;
import jrm.ui.progress.ProgressHandler;
import jrm.xml.EnhancedXMLStreamWriter;
import jrm.xml.SimpleAttribute;

public class Dir2Dat
{
	public Dir2Dat(File srcdir, File dstdat, final ProgressHandler progress, EnumSet<Options> options, ExportType type, HashMap<String, String> headers)
	{
		DirScan srcdir_scan  = new DirScan(srcdir, progress, options);
		write(dstdat, srcdir_scan, progress, options, type, headers);
	}

	private void write(final File dstdat, final DirScan scan, final ProgressHandler progress, EnumSet<Options> options, final ExportType type, HashMap<String, String> headers)
	{
		progress.clearInfos();
		progress.setInfos(1, false);
		AtomicInteger i = new AtomicInteger();
		scan.getContainersIterable().forEach(c->i.incrementAndGet());
		progress.setProgress("Saving...", 0, i.get());
		i.set(0);
		try(BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(dstdat)))
		{
			final EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(fos, "UTF-8")); //$NON-NLS-1$
			writer.writeStartDocument("UTF-8","1.0"); //$NON-NLS-1$ //$NON-NLS-2$
			switch(type)
			{
				case MAME:
				{
					writer.writeDTD("<!DOCTYPE mame [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/mame.dtd"), Charset.forName("UTF-8")) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					writer.writeStartElement("mame"); //$NON-NLS-1$
					Map<String,AtomicInteger> counter = new HashMap<>();
					for(Container c : scan.getContainersIterable())
					{
						String name = FilenameUtils.removeExtension(c.file.getName());
						Machine machine = null;
						if(Profile.curr_profile != null &&  options.contains(Options.MATCH_PROFILE))
							machine = Profile.curr_profile.machinelist_list.get(0).getByName(name);
						if(machine != null)
							name = machine.getBaseName();
						if(!counter.containsKey(name))
							counter.put(name, new AtomicInteger());
						AtomicInteger val = counter.get(name);
						if(val.incrementAndGet() > 1)
							name = name + "_" + val.get();
						progress.setProgress("Saving...", i.incrementAndGet());
						writer.writeStartElement("machine", //$NON-NLS-1$
							new SimpleAttribute("name", name), //$NON-NLS-1$
							new SimpleAttribute("isbios", machine!=null&&machine.isBios()?"yes":null), //$NON-NLS-1$
							new SimpleAttribute("isdevice", machine!=null&&machine.isdevice?"yes":null), //$NON-NLS-1$
							new SimpleAttribute("ismechanical", machine!=null&&machine.ismechanical?"yes":null), //$NON-NLS-1$
							new SimpleAttribute("cloneof", machine!=null?machine.cloneof:null), //$NON-NLS-1$
							new SimpleAttribute("romof", machine!=null?machine.romof:null), //$NON-NLS-1$
							new SimpleAttribute("sampleof", machine!=null?machine.sampleof:null) //$NON-NLS-1$
						);
						writer.writeElement("description", machine!=null?machine.description:name); //$NON-NLS-1$
						writer.writeElement("year", machine!=null?machine.year:"????"); //$NON-NLS-1$
						writer.writeElement("manufacturer", machine!=null?machine.manufacturer:""); //$NON-NLS-1$
						for(Entry e : c.getEntries())
						{
							if(e.type==Type.CHD)
							{
								String ename = normalize(FilenameUtils.removeExtension(e.getName()));
								if(options.contains(Options.JUNK_SUBFOLDERS))
									ename = Paths.get(ename).getFileName().toString();
								writer.writeElement("disk", //$NON-NLS-1$
									new SimpleAttribute("name", ename), //$NON-NLS-1$
									new SimpleAttribute("md5", e.md5), //$NON-NLS-1$
									new SimpleAttribute("sha1", e.sha1) //$NON-NLS-1$
								);
							}
							else
							{
								String ename = normalize(e.getName());
								if(options.contains(Options.JUNK_SUBFOLDERS))
									ename = Paths.get(ename).getFileName().toString();
								writer.writeElement("rom", //$NON-NLS-1$
									new SimpleAttribute("name", ename), //$NON-NLS-1$
									new SimpleAttribute("size", e.size), //$NON-NLS-1$
									new SimpleAttribute("crc", e.crc), //$NON-NLS-1$
									new SimpleAttribute("md5", options.contains(Options.NEED_MD5)?e.md5:null), //$NON-NLS-1$
									new SimpleAttribute("sha1", options.contains(Options.NEED_SHA1)?e.sha1:null) //$NON-NLS-1$
								);
							}
						}
						writer.writeEndElement();
					}
					writer.writeEndElement();
					break;
				}
				case DATAFILE:
				{
					writer.writeDTD("<!DOCTYPE datafile [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/datafile.dtd"), Charset.forName("UTF-8")) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					writer.writeStartElement("datafile"); //$NON-NLS-1$
					writer.writeStartElement("header");
					for(Map.Entry<String, String> entry:headers.entrySet())
						writer.writeElement(entry.getKey(), entry.getValue());
					writer.writeEndElement();
					Map<String,AtomicInteger> counter = new HashMap<>();
					for(Container c : scan.getContainersIterable())
					{
						String name = FilenameUtils.removeExtension(c.file.getName());
						Machine machine = null;
						if(Profile.curr_profile != null &&  options.contains(Options.MATCH_PROFILE))
							machine = Profile.curr_profile.machinelist_list.get(0).getByName(name);
						if(machine != null)
							name = machine.getBaseName();
						if(!counter.containsKey(name))
							counter.put(name, new AtomicInteger());
						AtomicInteger val = counter.get(name);
						if(val.incrementAndGet() > 1)
							name = name + "_" + val.get();
						progress.setProgress("Saving...", i.incrementAndGet());
						writer.writeStartElement("game", //$NON-NLS-1$
							new SimpleAttribute("name", name), //$NON-NLS-1$
							new SimpleAttribute("isbios", machine!=null&&machine.isBios()?"yes":null), //$NON-NLS-1$
							new SimpleAttribute("cloneof", machine!=null?machine.cloneof:null), //$NON-NLS-1$
							new SimpleAttribute("romof", machine!=null?machine.romof:null), //$NON-NLS-1$
							new SimpleAttribute("sampleof", machine!=null?machine.sampleof:null) //$NON-NLS-1$
						);
						writer.writeElement("description", machine!=null?machine.description:name); //$NON-NLS-1$
						writer.writeElement("year", machine!=null?machine.year:"????"); //$NON-NLS-1$
						writer.writeElement("manufacturer", machine!=null?machine.manufacturer:""); //$NON-NLS-1$
						for(Entry e : c.getEntries())
						{
							if(e.type==Type.CHD)
							{
								String ename = normalize(FilenameUtils.removeExtension(e.getName()));
								if(options.contains(Options.JUNK_SUBFOLDERS))
									ename = Paths.get(ename).getFileName().toString();
								writer.writeElement("disk", //$NON-NLS-1$
									new SimpleAttribute("name", ename), //$NON-NLS-1$
									new SimpleAttribute("md5", e.md5), //$NON-NLS-1$
									new SimpleAttribute("sha1", e.sha1) //$NON-NLS-1$
								);
							}
							else
							{
								String ename = normalize(e.getName());
								if(options.contains(Options.JUNK_SUBFOLDERS))
									ename = Paths.get(ename).getFileName().toString();
								writer.writeElement("rom", //$NON-NLS-1$
									new SimpleAttribute("name", ename), //$NON-NLS-1$
									new SimpleAttribute("size", e.size), //$NON-NLS-1$
									new SimpleAttribute("crc", e.crc), //$NON-NLS-1$
									new SimpleAttribute("md5", options.contains(Options.NEED_MD5)?e.md5:null), //$NON-NLS-1$
									new SimpleAttribute("sha1", options.contains(Options.NEED_SHA1)?e.sha1:null), //$NON-NLS-1$
									new SimpleAttribute("offset", 0), //$NON-NLS-1$
									new SimpleAttribute("date", e.modified) //$NON-NLS-1$
								);
							}
						}
						writer.writeEndElement();
					}
					writer.writeEndElement();
					break;
				}
				case SOFTWARELIST:
				{
					Map<String,Map<String,AtomicInteger>> slcounter = new HashMap<>();
					Map<String,SL> slmap = new HashMap<>();
					for(Container c : scan.getContainersIterable())
					{
						progress.setProgress("Saving...", i.incrementAndGet());
						Software software = null;
						Path relativized = scan.getDir().toPath().relativize(c.file.toPath());
						String swname = FilenameUtils.removeExtension(relativized.getFileName().toString());
						String slname = relativized.getParent().toString();
						if(Profile.curr_profile!= null)
						{
							SoftwareList sl = Profile.curr_profile.machinelist_list.softwarelist_list.getByName(slname);
							if(sl != null && sl.containsName(swname))
								software = sl.getByName(swname);
							if(software != null && options.contains(Options.MATCH_PROFILE))
							{
								swname = software.getBaseName();
								slname = software.sl.getBaseName();
							}
						}
						if(!slcounter.containsKey(slname))
						{
							slcounter.put(slname, new HashMap<>());
							slmap.put(slname, new SL(slname,software!=null?software.sl:null));
						}
						Map<String, AtomicInteger> swcounter = slcounter.get(slname);
						if(!swcounter.containsKey(swname))
							swcounter.put(swname, new AtomicInteger());
						AtomicInteger val = swcounter.get(swname);
						if(val.incrementAndGet() > 1)
							swname = swname + "_" + val.get();
						slmap.get(slname).sw.put(swname,new SL.SW(swname, software, c));
					}
					if(slmap.size()>1)
					{
						writer.writeDTD("<!DOCTYPE softwarelists [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/softwarelists.dtd"), Charset.forName("UTF-8")) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						writer.writeStartElement("softwarelists"); //$NON-NLS-1$
					}
					for(Map.Entry<String,SL> e : slmap.entrySet())
					{
						if(slmap.size()==1)
							writer.writeDTD("<!DOCTYPE softwarelist [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/softwarelist.dtd"), Charset.forName("UTF-8")) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						writer.writeStartElement("softwarelist",
							new SimpleAttribute("name", e.getValue().name)
						);
						if(e.getValue().sl!=null)
							writer.writeElement("description", e.getValue().sl.description);
						for(Map.Entry<String, SL.SW> ee : e.getValue().sw.entrySet())
						{
							if(ee.getValue().sw!=null)
								ee.getValue().sw.export(writer, ee.getValue().c.getEntries());
							else
							{
								writer.writeStartElement("software",
									new SimpleAttribute("name", ee.getValue().name)
								);
								int ii = 0;
								for(Entry entry : ee.getValue().c.getEntries())
								{
									if(entry.type==Type.CHD)
									{
										String ename = normalize(FilenameUtils.removeExtension(entry.getName()));
										if(options.contains(Options.JUNK_SUBFOLDERS))
											ename = Paths.get(ename).getFileName().toString();
										writer.writeStartElement("part", //$NON-NLS-1$
											new SimpleAttribute("name", "cdrom"+ ++ii), //$NON-NLS-1$
											new SimpleAttribute("interface", "cdrom") //$NON-NLS-1$
										);
											writer.writeStartElement("diskarea", //$NON-NLS-1$
												new SimpleAttribute("name", "cdrom") //$NON-NLS-1$
											);
												writer.writeElement("disk", //$NON-NLS-1$
														new SimpleAttribute("name", ename), //$NON-NLS-1$
														new SimpleAttribute("sha1", entry.sha1) //$NON-NLS-1$
													);
											writer.writeEndElement();
										writer.writeEndElement();
									}
									else
									{
										String ename = normalize(entry.getName());
										if(options.contains(Options.JUNK_SUBFOLDERS))
											ename = Paths.get(ename).getFileName().toString();
										writer.writeStartElement("part", //$NON-NLS-1$
											new SimpleAttribute("name", "flop"+ ++ii), //$NON-NLS-1$
											new SimpleAttribute("interface", "floppy_3_5") //$NON-NLS-1$
										);
											writer.writeStartElement("dataarea", //$NON-NLS-1$
												new SimpleAttribute("name", "flop"), //$NON-NLS-1$
												new SimpleAttribute("size", entry.size) //$NON-NLS-1$
											);
												writer.writeElement("rom", //$NON-NLS-1$
													new SimpleAttribute("name", ename), //$NON-NLS-1$
													new SimpleAttribute("size", entry.size), //$NON-NLS-1$
													new SimpleAttribute("crc", entry.crc), //$NON-NLS-1$
													new SimpleAttribute("sha1", options.contains(Options.NEED_SHA1)?entry.sha1:null) //$NON-NLS-1$
												);
											writer.writeEndElement();
										writer.writeEndElement();
										
									}
								}
								writer.writeEndElement();
							}
						}
						writer.writeEndElement();
					}
					if(slmap.size()>1)
						writer.writeEndElement();
					break;
				}
			}
			writer.writeEndDocument();
			writer.close();
		}
		catch(FactoryConfigurationError | XMLStreamException | IOException e)
		{
			JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			e.printStackTrace();
		}
	}
	
	private static class SL
	{
		private static class SW
		{
			private Software sw = null;
			private Container c = null;
			private String name;
			
			private SW(String name, Software sw, Container c)
			{
				this.name = name;
				this.sw = sw;
				this.c  = c;
			}
		}
		
		private SL(String name, SoftwareList sl)
		{
			this.name = name;
			this.sl = sl;
		}
		
		private SoftwareList sl = null;
		private String name;
		private HashMap<String, SW> sw = new HashMap<>();
	}
	
	/**
	 * Normalize char separator according platform default separator
	 * @param entry the entry to normalize
	 * @return the normalized entry
	 */
	private String normalize(final String entry)
	{
		if(File.separatorChar == '/')
			return entry.replace('\\', '/');
		return entry.replace('/', '\\');
	}
}