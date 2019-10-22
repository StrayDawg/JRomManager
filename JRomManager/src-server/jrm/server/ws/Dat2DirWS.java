package jrm.server.ws;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import jrm.batch.DirUpdater;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.Options;
import jrm.misc.ProfileSettings;
import jrm.server.WebSession;
import jrm.ui.basic.ResultColUpdater;
import jrm.ui.basic.SrcDstResult;

public class Dat2DirWS
{
	private final WebSckt ws;

	public Dat2DirWS(WebSckt ws)
	{
		this.ws = ws;
	}

	void start(JsonObject jso)
	{
		(ws.session.worker = new Worker(()->{
			WebSession session = ws.session;
			boolean dryrun = session.getUser().settings.getProperty(Options.dat2dir_dry_run, true);
			session.worker.progress = new ProgressWS(ws);
			try
			{
				String[] srcdirs = StringUtils.split(session.getUser().settings.getProperty(Options.dat2dir_srcdirs, ""),'|');
				if (srcdirs.length > 0)
				{
					List<SrcDstResult> sdrl =  SrcDstResult.fromJSON(session.getUser().settings.getProperty(Options.dat2dir_sdr, "[]"));
					if (sdrl.stream().filter((sdr) -> !session.getUser().settings.getProfileSettingsFile(sdr.src).exists()).count() > 0)
						new GlobalWS(ws).warn(ws.session.msgs.getString("MainFrame.AllDatsPresetsAssigned")); //$NON-NLS-1$
					else
					{
						new DirUpdater(session, sdrl, session.worker.progress, Stream.of(srcdirs).map(s->new File(s)).collect(Collectors.toList()), new ResultColUpdater()
						{
							@Override
							public void updateResult(int row, String result)
							{
								sdrl.get(row).result = result;
								session.getUser().settings.setProperty(Options.dat2dir_sdr, SrcDstResult.toJSON(sdrl));
								Dat2DirWS.this.updateResult(row, result);
							}
							
							@Override
							public void clearResults()
							{
								sdrl.forEach(sdr -> sdr.result = "");
								session.getUser().settings.setProperty(Options.dat2dir_sdr, SrcDstResult.toJSON(sdrl));
								Dat2DirWS.this.clearResults();
							}
						}, dryrun);
					}
				}
				else
					new GlobalWS(ws).warn(ws.session.msgs.getString("MainFrame.AtLeastOneSrcDir"));
			}
			catch(BreakException e)
			{
				
			}
			finally
			{
				Dat2DirWS.this.end();
				session.curr_profile = null;
				session.curr_scan = null;
				session.worker.progress.close();
				session.worker.progress = null;
				session.lastAction = new Date();
			}
		})).start();
	}

	@SuppressWarnings("serial")
	void settings(JsonObject jso)
	{
		JsonArray srcs = jso.get("params").asObject().get("srcs").asArray();
		if(srcs!=null && srcs.size()>0)
		{
			String src = srcs.get(0).asString();
			try
			{
				ProfileSettings settings = ws.session.getUser().settings.loadProfileSettings(new File(src), null);
				if(ws.isOpen())
				{
					ws.send(new JsonObject() {{
						add("cmd", "Dat2Dir.showSettings");
						add("params", new JsonObject() {{
							add("settings", settings.asJSO());
							add("srcs",srcs);
						}});
					}}.toString());
				}
			}
			catch (IOException e)
			{
				Log.err(e.getMessage(),e);
			}
		}
	}
	
	
	@SuppressWarnings("serial")
	void updateResult(int row, String result)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Dat2Dir.updateResult");
					add("params", new JsonObject() {{
						add("row", row);
						add("result", result);
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	@SuppressWarnings("serial")
	void clearResults()
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Dat2Dir.clearResults");
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	@SuppressWarnings("serial")
	void end()
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Dat2Dir.end");
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

}
