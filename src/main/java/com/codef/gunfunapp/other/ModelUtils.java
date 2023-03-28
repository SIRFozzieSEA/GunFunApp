package com.codef.gunfunapp.other;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import com.codef.gunfunapp.models.entities.Registry;
import com.codef.gunfunapp.models.entities.TriviaRoundQuestion;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ModelUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtils.class);

	public static boolean BUILD_JSON = false;
	public static boolean OUTPUT_JSON = false;

	private Model model;
	private ObjectMapper mapper;
	private ObjectNode rootNode;

	public ModelUtils(Model model) {
		this.model = model;
		this.mapper = new ObjectMapper();
		this.rootNode = mapper.createObjectNode();
	}

	@SuppressWarnings("unchecked")
	public void addToModel(String key, Object objValue) {
		this.model.addAttribute(key, objValue);

		if (BUILD_JSON) {
			if (objValue == null) {
				this.rootNode.put(key, "");
			} else if (objValue instanceof String) {
				this.rootNode.put(key, objValue.toString());
			} else if (objValue instanceof Long) {
				this.rootNode.put(key, (Long) objValue);
			} else if (objValue instanceof Boolean) {
				this.rootNode.put(key, (Boolean) objValue);
			} else if (objValue instanceof Double) {
				this.rootNode.put(key, (Double) objValue);
			} else if (objValue instanceof Integer) {
				this.rootNode.put(key, (Integer) objValue);
			} else if (objValue instanceof Date) {
				this.rootNode.put(key, objValue.toString());
			} else if (objValue instanceof TriviaRoundQuestion) {
				this.rootNode.set(key, genericiseSimpleObject((TriviaRoundQuestion) objValue));
			} else if (objValue instanceof Registry) {
				this.rootNode.set(key, genericiseSimpleObject((Registry) objValue));
			} else {
				if (key.endsWith("_TreeSetString")) {
					ArrayNode arrayNode = mapper.createArrayNode();
					for (String singleValue : (TreeSet<String>) objValue) {
						arrayNode.add(singleValue);
					}
					this.rootNode.set(key, arrayNode);
				} else if (key.endsWith("_ArrayListString")) {
					ArrayNode arrayNode = mapper.createArrayNode();
					for (String singleValue : (ArrayList<String>) objValue) {
						arrayNode.add(singleValue);
					}
					this.rootNode.set(key, arrayNode);
				} else if (key.endsWith("_ArrayListDate")) {
					ArrayNode arrayNode = mapper.createArrayNode();
					for (Date singleValue : (ArrayList<Date>) objValue) {
						arrayNode.add(singleValue.toString());
					}
					this.rootNode.set(key, arrayNode);
				} else if (key.endsWith("_ArrayListHashMapStringString")) {
					ArrayNode arrayNode = mapper.createArrayNode();
					for (HashMap<String, String> singleHashMap : (ArrayList<HashMap<String, String>>) objValue) {
						ObjectNode objectRow = mapper.createObjectNode();
						for (Map.Entry<String, String> mapElement : singleHashMap.entrySet()) {
							objectRow.put(mapElement.getKey(), mapElement.getValue());
						}
						arrayNode.add(objectRow);
					}
					this.rootNode.set(key, arrayNode);
				} else if (key.endsWith("_ArrayListArrayListHashMapStringString")) {
					// This object is only needed in order to display in two columns, at some
					// point this could be a _ArrayListHashMapStringString if the presentation
					// is fixed not to run the values from two seperate columns
					ArrayNode arrayNodeOutside = mapper.createArrayNode();
					for (ArrayList<HashMap<String, String>> insideArrayList : (ArrayList<ArrayList<HashMap<String, String>>>) objValue) {
						ArrayNode arrayNodeInside = mapper.createArrayNode();
						for (HashMap<String, String> singleHashMap : (ArrayList<HashMap<String, String>>) insideArrayList) {
							ObjectNode objectRow = mapper.createObjectNode();
							for (Map.Entry<String, String> mapElement : singleHashMap.entrySet()) {
								objectRow.put(mapElement.getKey(), mapElement.getValue());
							}
							arrayNodeInside.add(objectRow);
						}
						arrayNodeOutside.add(arrayNodeInside);
					}
					this.rootNode.set(key, arrayNodeOutside);
				} else {
					LOGGER.error("Model named '" + key + "' is unsupported type in genericiseSimpleObject()");
				}
			}
		}

	}

	public ObjectNode genericiseSimpleObject(Object obj) {
		ObjectNode objectRow = mapper.createObjectNode();
		Class<?> clazz = obj.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				String fieldName = field.getName();
				Object fieldObject = field.get(obj);

				if (fieldObject == null) {
					objectRow.put(fieldName, "");
				} else if (fieldObject instanceof String) {
					objectRow.put(fieldName, fieldObject.toString());
				} else if (fieldObject instanceof Long) {
					objectRow.put(fieldName, (Long) fieldObject);
				} else if (fieldObject instanceof Boolean) {
					objectRow.put(fieldName, (Boolean) fieldObject);
				} else if (fieldObject instanceof Double) {
					objectRow.put(fieldName, (Double) fieldObject);
				} else if (fieldObject instanceof Integer) {
					objectRow.put(fieldName, (Integer) fieldObject);
				} else if (fieldObject instanceof Date) {
					objectRow.put(fieldName, fieldObject.toString());
				} else if (fieldObject instanceof BigDecimal) {
					BigDecimal tempDecimal = (BigDecimal) fieldObject;
					objectRow.put(fieldName, tempDecimal.doubleValue());
				} else {
					LOGGER.error("Field '" + fieldName + "' is unsupported type in genericiseSimpleObject()");
				}

			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return objectRow;
	}

	public Model getModel() {
		return this.model;
	}

	@Override
	public String toString() {
		return "ModelUtils [model=" + this.model.toString() + "]";
	}

	public void printJson() {
		if (OUTPUT_JSON) {
			// System.out.println(this.model.toString());
			System.out.println(toJson());
			System.out.println("");
			System.out.println("-----------------------------------------------------");
		}
	}

	public String toJson() {
		try {
			return this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.rootNode);
		} catch (JsonProcessingException e) {
			return "{ \"error\": \"toJson() error\" }";
		}
	}

}
