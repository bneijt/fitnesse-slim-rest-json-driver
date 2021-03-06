package net.zesc.fitnesse.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class ResponseJsonWrapper {

	private final HttpResponse response;

	public ResponseJsonWrapper(HttpResponse httpResponse) {
		super();
		this.response = httpResponse;
	}

	public JSONObject toJson() {
		try {
			return new JSONObject().put("status", getStatusJson())
					.put("header", getHeaderJson()).put("body", getBodyJson());
		} catch (JSONException e) {
			// let fitnesse show the exception.
			throw new RuntimeException(e);
		}
	}

	private Object getBodyJson() throws JSONException {
		String bodyString = getBodyString();
		if (bodyString == null) {
			return new JSONObject();
		}
		try {
			if (bodyString.startsWith("[")) {
				return new JSONArray(bodyString);
			} else if (bodyString.isEmpty()) {
				return new JSONObject();
			} else {
				return new JSONObject(bodyString);
			}
			// if string is not json add it as txt
		} catch (JSONException e) {
			// do nothing an error will be printed to sysout and added to JSON.
		}
		System.out.println(bodyString);
		return new JSONObject().put("error",
				"No JSON body. Check system output");
	}

	private JSONObject getStatusJson() throws JSONException {
		return new JSONObject().put("code",
				response.getStatusLine().getStatusCode()).put("reason",
				response.getStatusLine().getReasonPhrase());
	}

	private JSONObject getHeaderJson() throws JSONException {
		Header[] allHeaders = response.getAllHeaders();
		JSONObject jsonObject = new JSONObject();
		for (Header header : allHeaders) {
			jsonObject.put(header.getName(), header.getValue());
		}
		return jsonObject;
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	private String getBodyString() {
		String content = null;
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			try {
				content = getContentAsString(entity);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return content;
	}

	private String getContentAsString(HttpEntity entity) throws IOException {
		String content;
		InputStream inStream = entity.getContent();
		try {
			content = convertStreamToString(inStream);
		} finally {
			inStream.close();
		}
		return content;
	}

	private String convertStreamToString(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		String NL = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			sb.append(line).append(NL);
		}
		return sb.toString();
	}
}
