package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.util.Base64;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import java.lang.reflect.Type;

//all these imports are just for isEmpty
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
	
	private final Gson gson = new GsonBuilder()
			.disableHtmlEscaping().setPrettyPrinting()
			.registerTypeAdapter(byte[].class, new ByteArrayAdapter())
			.create();
	private final File baseDir;
	
    public DocumentPersistenceManager(File baseDir){
    	this.baseDir = baseDir == null ? new File(System.getProperty("user.dir")) : baseDir;
    	if (!this.baseDir.canWrite()||!this.baseDir.canRead()) {
    		throw new IllegalStateException();
    	}
    }

    public void serialize(URI uri, Document val) throws IOException {
		File dir = getFile(uri);
		dir.getParentFile().mkdirs();
    	try (Writer writer = new FileWriter(dir)) {
		    gson.toJson(val, writer);
    	} catch (Exception e) {
    		throw new IllegalArgumentException(e);
    	}
    }

    public Document deserialize(URI uri) throws IOException {
		File dir = getFile(uri);
    	try (FileReader fileReader = new FileReader(dir)) {
			Document returnDoc = gson.fromJson(fileReader, DocumentImpl.class);
			fileReader.close();
			delete(uri);
			return returnDoc;
    	} catch (Exception e) {
    		return null;
    	}
    }

    public boolean delete(URI uri) throws IOException {
    	try {
    		File dir = getFile(uri);
    		//delete only fails if file can't be deleted
    		if (!dir.delete()) {
    			return false;
    		}
    		dir = dir.getParentFile();
    		while (!dir.equals(baseDir) && isEmpty(dir)) {
        		dir.delete();
				dir = dir.getParentFile();
			}
    		return true;
    	} catch (Exception e) {
    		return false;
    	}
    }
    
//there are no IO operations that can check if a directory is empty in a reasonable amount of time
//File.listFiles lists all files in a directory which is an expensive op if there are a lot of files
//directoryStream lazily lists files which is a much cheaper op
//hate to mix nio and io methods, but little choice here
    public boolean isEmpty(File file) throws IOException {
        if (Files.isDirectory(file.toPath())) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(file.toPath())) {
                return !directory.iterator().hasNext();
            }
        }
        return false;
    }
    
    private File getFile(URI uri) {
    	return new File(baseDir.toString() + File.separatorChar +
    	((uri.getHost() != null ? uri.getHost() : "") +
    	(uri.getPath() != null ? uri.getPath() : "")) + ".json");
    }
    
    private class ByteArrayAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
    	
    	public JsonElement serialize(byte[] bitArray, Type type, JsonSerializationContext context) {
    		return new JsonPrimitive(Base64.getEncoder().encodeToString(bitArray));
    	}
    	
    	public byte[] deserialize (JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    		return Base64.getDecoder().decode(json.getAsString());
    	}
    }
}