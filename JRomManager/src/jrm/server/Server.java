package jrm.server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fi.iki.elonen.NanoWSD.WebSocket;
import jrm.security.Session;
import jrm.server.handlers.DataSourcesHandler;
import jrm.server.handlers.ResourceHandler;
import jrm.server.handlers.SessionHandler;
import jrm.server.handlers.EnhStaticPageHandler;
import jrm.server.ws.WebSckt;

public class Server extends EnhRouterNanoHTTPD implements SessionStub
{
	private String clientPath;
	
	public Server(int port, String clientPath)
	{
		super(port);
		this.clientPath = clientPath;
		addMappings();
	}

	/**
	 * Main entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		Options options = new Options();
		options.addOption(new Option("c", "client", true, "Client Path"));
		options.addOption(new Option("p", "port", true, "Server Port"));
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		String clientPath = null;
		int port = 8080;
		try
		{
			cmd = parser.parse(options, args);
			if (null == (clientPath = cmd.getOptionValue('c')))
			{
				try
				{
					clientPath = new File(new File(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI()), "smartgwt").getPath();
				}
				catch (URISyntaxException e)
				{
					e.printStackTrace();
				}
			}
			try
			{
				if(cmd.hasOption('p'))
					port = Integer.parseInt(cmd.getOptionValue('p'));
			}
			catch (NumberFormatException e)
			{
			}
		}
		catch (ParseException e)
		{
			System.out.println(e.getMessage());
			formatter.printHelp("Server", options);
			System.exit(1);
		}
		try
		{
			Locale.setDefault(Locale.US);
			Server server = new Server(port, clientPath);
			server.start(0);
			try
			{
				System.out.println("Start server");
				System.out.println("port: "+port);
				System.out.println("clientPath: "+clientPath);
				System.in.read();
			}
			catch (Throwable ignored)
			{
			}
	        server.stop();
	        System.out.println("Server stopped.\n");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void addMappings()
	{
		super.addMappings();
		addRoute("/", jrm.server.handlers.IndexHandler.class);
		addRoute("/index.html", jrm.server.handlers.IndexHandler.class);
		addRoute("/smartgwt/(.)+", EnhStaticPageHandler.class, new File(clientPath));
		addRoute("/images/(.)+", ResourceHandler.class, Server.class.getResource("/jrm/resources/"));
		addRoute("/datasources/:action/", DataSourcesHandler.class);
		addRoute("/session/", SessionHandler.class, this);
	}
	
	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake)
	{
		return new WebSckt(this, handshake);
	}

	final static Map<String, Session> sessions = new HashMap<>();
	
	private Session session = null;
	
	@Override
	public Session getSession()
	{
		return session;
	}

	public static Session getSession(String session)
	{
		return sessions.get(session);
	};

	@Override
	public void setSession(Session session)
	{
		this.session = session;
		sessions.put(session.getSessionId(), session);
	}

	@Override
	public void unsetSession(Session session)
	{
		sessions.remove(session.getSessionId());
		this.session = null;
	}
}