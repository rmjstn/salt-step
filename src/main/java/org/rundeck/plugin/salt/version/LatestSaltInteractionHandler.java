package org.rundeck.plugin.salt.version;

import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.rundeck.plugin.salt.SaltApiException;
import org.rundeck.plugin.salt.output.SaltApiResponseOutput;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The latest incarnation of the interaction handler.
 */
public class LatestSaltInteractionHandler implements SaltInteractionHandler {

    protected static final String SALT_OUTPUT_RETURN_KEY = "return";
    protected static final Type MINION_RESPONSE_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
    protected static final Type LIST_OF_SALT_API_RESPONSE_TYPE = new TypeToken<List<SaltApiResponseOutput>>() {}.getType();

    @Override
    public SaltApiResponseOutput extractOutputForJobSubmissionResponse(String json) throws SaltApiException {
        /**
         * The response currently looks like: {"_links": {"jobs": [{"href":
         * "/jobs/20130903200912838566"}]}, "return": [{"jid":
         * "20130903200912838566", "minions": ["host1", "host2"]}]}
         */
		System.out.println("LatestSaltInteractionHandler json : " + json);
        Gson gson = new Gson();
        Map<String, Object> responses = gson.fromJson(json, MINION_RESPONSE_TYPE);
		System.out.println("responses : " + responses);
		System.out.println("responses.get(SALT_OUTPUT_RETURN_KEY).toString() : " + responses.get(SALT_OUTPUT_RETURN_KEY).toString());
        List<SaltApiResponseOutput> saltOutputs = gson.fromJson(responses.get(SALT_OUTPUT_RETURN_KEY).toString(),
                                                                LIST_OF_SALT_API_RESPONSE_TYPE);
        if (saltOutputs.size() != 1) {
            throw new SaltApiException(String.format("Could not understand salt response %s", json));
        }
        
        return saltOutputs.get(0);
    }

	@Override
	public String extractRetCodeForJobSubmissionResponse(String json) throws SaltApiException {
        Gson gson = new Gson();
	    Object result = gson.fromJson(json, Object.class);
	
        List<String> results=getValues(result, "retcode");
		if (results.size() == 0) {
			throw new SaltApiException(String.format("Could not understand salt response %s", json));
		}
						
        return results.get(0);
    }
	
	private List getValues(Object object, String attribute) {
        ArrayList<String> attributeValues = new ArrayList<String>();
		if (object instanceof Map) {
			Map map = (Map) object;
			for ( String key : (Set<String>)map.keySet() ) { 
				if (attribute.equalsIgnoreCase(key)) {
					attributeValues.add(map.get(key).toString());
				} 
            }
			Collection values = map.values();
			for (Object value : values)
				attributeValues.addAll(getValues(value, attribute));
	    }
		else if (object instanceof Collection) {
			Collection values = (Collection) object;
			for (Object value : values) {
				attributeValues.addAll(getValues(value, attribute));
			}
		}
        return attributeValues;
    }	
}
