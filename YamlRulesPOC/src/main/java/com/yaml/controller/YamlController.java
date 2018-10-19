package com.yaml.controller;



import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.mvel.MVELRuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rules.model.RuleData;
import com.yaml.model.LoanDetails;
import com.yaml.model.ProcessResponse;
import com.yaml.model.Rules;
import com.yaml.model.SelectionCriteria;
import com.yaml.model.ValuePairs;
import com.yaml.model.YamlRequest;
import com.yaml.model.YamlResponse;
import com.yaml.utils.ResourceProperties;

@RestController
public class YamlController {
	
	Logger logger = LoggerFactory.getLogger(YamlController.class);
	
	@Autowired ResourceProperties resourceProperties;
		
	@RequestMapping(value="/createRules",method=RequestMethod.POST,produces="application/json", consumes="application/json")
	public YamlResponse recieveRules(@RequestBody YamlRequest request){
		YamlResponse response = new YamlResponse();
		FileWriter writer =  null;
		
		if(request != null) {
			logger.info("request has "+request.getObligationId());
			logger.info("request  selection critiria"+request.getSelectionCriteria().size());
			
			String yamlContent = getYamlParser().dumpAll(mapRulesData(request.getSelectionCriteria()).iterator());
			
			try {
				writer = new FileWriter("./src/main/resources/"+getFileName(request.getObligationId()));
				logger.info("Generated yaml content"+yamlContent);
				writer.write(yamlContent);
				writer.close();
			}catch(IOException ioe) {
				logger.info("File not found or unable to create one");
				response.setProcess("Yaml file creation failure");
				response.setStatus("failure");
			}
			
			logger.info("Yaml Output "+writer.toString());
			writeToJsonFile(request);
			response.setProcess("Yaml and JSON File Generated Successfully");
			response.setStatus("success");
		}else {
			response.setProcess("missing");
			response.setStatus("failure");
		}
		
		return response;
	}
	
	@RequestMapping(value="/executeRules",method=RequestMethod.POST,produces="application/json", consumes="application/json")
	public ProcessResponse executeRules(@RequestBody LoanDetails loanDetails){
		Facts facts = new Facts();
		ProcessResponse response = new ProcessResponse();
		List<ValuePairs> valuePairs = new ArrayList<ValuePairs>();
		facts.put("loanDetails", loanDetails);
        
        try {
        	org.jeasy.rules.api.Rules rules = MVELRuleFactory.createRulesFrom(new FileReader("./src/main/resources/"+getFileName(loanDetails.getObligationId())));
	        
	        RulesEngine rulesEngine = new DefaultRulesEngine();
	        rulesEngine.fire(rules, facts);
	        
	        valuePairs.add(getValuePair("mortgageRate",facts.get("mortgageRate")));
	        valuePairs.add(getValuePair("commisionRate",facts.get("commisionRate")));
	        response.setStatus("success");
	        response.setValuePairs(valuePairs);
        }catch(IOException ioe) {
        	response.setStatus("failure");
        	logger.info("Exception while reading the file"+ioe);
        }
		return response;
	}
	
	
	private boolean writeToJsonFile(YamlRequest request) {
		ObjectMapper jsonMapper = new ObjectMapper();
		try {
			jsonMapper.writeValue(new FileWriter("./src/main/resources/output.json"), request);
		}catch(Exception e) {
			logger.info("exception while mapping to json"+e);
		}
		return false;
	}
	
	
	private List<RuleData> mapRulesData(List<SelectionCriteria> selections) {
		List<RuleData> rulesData = new ArrayList<RuleData>();
		RuleData ruleData = null;
		List<String> actions = null;
		StringBuffer formRule;
		for(SelectionCriteria criteira:selections) {
			formRule = new StringBuffer();
			ruleData = new RuleData();
			ruleData.setName(criteira.getGroupName());
			Iterator<Rules> iterator = criteira.getRules().iterator();
			while(iterator.hasNext()) {
				Rules rule = iterator.next();
				formRule.append(resourceProperties.getPropertyValue(rule.getKey()));
				formRule.append(rule.getOperator());
				formRule.append(rule.getValue());
				if(iterator.hasNext()) {
					formRule.append(resourceProperties.getPropertyValue(rule.getCondition()));
				}
			}
			ruleData.setCondition(formRule.toString());
			actions = new ArrayList<String>();
			if("group 1".equalsIgnoreCase(criteira.getGroupName())) {
				actions.add("mortgageRate=2.0");
				actions.add("commisionRate=6.0");
			}else {
				actions.add("mortgageRate=4.0");
				actions.add("commisionRate=7.0");
			}
			ruleData.setActions(actions);
			rulesData.add(ruleData);
		}
		return rulesData;
	}
	
	private Yaml getYamlParser() {
		Representer representer = new Representer();
		DumperOptions options = new DumperOptions(); 
		options.setExplicitStart(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		representer.addClassTag(RuleData.class, Tag.MAP);
		Yaml yamlPraser = new Yaml(representer, options);
		return yamlPraser;
		
	}
	
	private ValuePairs getValuePair(String key, Double value) {
		ValuePairs keyValue = new ValuePairs();
        keyValue.setKey(key);
        keyValue.setValue(Double.toString(value));
        return keyValue;
	}
	
	private String getFileName(String obligation) {
		StringBuffer fileName = new StringBuffer();
		fileName.append(obligation);
		fileName.append("-rules.yml");
		return fileName.toString();
	}

}
