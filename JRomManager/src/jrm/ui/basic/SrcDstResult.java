package jrm.ui.basic;

import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class SrcDstResult
{
	public String src = null;
	public String dst = null;
	public String result = ""; //$NON-NLS-1$
	public boolean selected = true;

	public SrcDstResult()
	{
	}

	public SrcDstResult(String src)
	{
		this.src= src;
	}

	public SrcDstResult(String src, String dst)
	{
		this.src= src;
		this.dst = dst;
	}

	public SrcDstResult(JsonObject jso)
	{
		fromJSONObject(jso);
	}
	

	@Override
	public boolean equals(Object obj)
	{
		if(obj != null && obj instanceof SrcDstResult)
			return src.equals(((SrcDstResult)obj).src);
		return false;
	}
	
	public JsonObject toJSONObject()
	{
		JsonObject jso = Json.object();
		jso.add("src", src != null ? src.toString() : null); //$NON-NLS-1$
		jso.add("dst", dst != null ? dst.toString() : null); //$NON-NLS-1$
		jso.add("result", result); //$NON-NLS-1$
		jso.add("selected", selected); //$NON-NLS-1$
		return jso;
	}
	
	public void fromJSONObject(JsonObject jso)
	{
		JsonValue src = jso.get("src"); //$NON-NLS-1$
		if (src != Json.NULL)
			this.src = src.asString();
		JsonValue dst = jso.get("dst"); //$NON-NLS-1$
		if (dst != Json.NULL)
			this.dst = dst.asString();
		this.result = jso.get("result").asString(); //$NON-NLS-1$
		this.selected = jso.getBoolean("selected", true); //$NON-NLS-1$
	}
	
	public static String toJSON(List<SrcDstResult> list)
	{
		JsonArray array = Json.array();
		for (SrcDstResult sdr : list)
			array.add(sdr.toJSONObject());
		return array.toString();
	}
	
	public static List<SrcDstResult> fromJSON(String json)
	{
		List<SrcDstResult> sdrl = new ArrayList<>();
		for (JsonValue arrv : Json.parse(json).asArray()) //$NON-NLS-1$ //$NON-NLS-2$
			sdrl.add(new SrcDstResult(arrv.asObject()));
		return sdrl;
	}
}