package jrm.server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.cli.*;

import fi.iki.elonen.NanoWSD.WebSocket;
import jrm.security.Session;
import jrm.server.handlers.DataSourcesHandler;
import jrm.server.handlers.ResourceHandler;
import jrm.server.handlers.SessionHandler;
import jrm.server.ws.WebSckt;

public class Server extends EnhRouterNanoHTTPD implements SessionStub
{
	private static String clientPath;
	private static int port = 8080;
	
	public Server()
	{
		super(Server.port);
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

		try
		{
			cmd = parser.parse(options, args);
			if (null == (Server.clientPath = cmd.getOptionValue('c')))
			{
				try
				{
					Server.clientPath = new File(new File(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI()), "smartgwt").getPath();
				}
				catch (URISyntaxException e)
				{
					e.printStackTrace();
				}
			}
			try
			{
				Server.port = Integer.parseInt(cmd.getOptionValue('p'));
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
			Server server = Server.class.newInstance();
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
		catch (InstantiationException | IllegalAccessException | IOException e)
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
		addRoute("/smartgwt/(.)+", StaticPageHandler.class, new File(Server.clientPath));
		addRoute("/images/(.)+", ResourceHandler.class, Server.class.getResource("/jrm/resources/"));
		addRoute("/datasources/:action/", DataSourcesHandler.class);
		addRoute("/session/", SessionHandler.class, this);
	}
	
	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake)
	{
		return new WebSckt(handshake);
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
