package io.pivotal.gemfire_addon.tools.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import com.gemstone.gemfire.cache.client.ClientCache;

import io.pivotal.gemfire_addon.tools.CommonImport;
import io.pivotal.gemfire_addon.tools.client.utils.Bootstrap;
import io.pivotal.gemfire_addon.types.ImportRequest;

/**
 * <P>
 * Common processing for client invoked data import. This varies slightly
 * depending if the import is local (from the client) or invoked from
 * the client on remote servers -- so these functions are abstract and
 * have to be provided with an appropriate implementation.
 * </P>
 */
public abstract class DataImport extends CommonImport {
	private 	boolean						error = false;
	private 	static ClientCache 			clientCache = null;
	private 	static final long 			globalStartTime = System.currentTimeMillis();

	protected abstract void usage();
	
	protected void process(final String[] args) throws Exception {
		int fileCount=0;
		
		if(args==null || args.length<2) {
			this.usage();
			this.error=true;
			return;
		}
		
		parseLocators(args[0]);

		clientCache = Bootstrap.createDynamicCache();
		super.setLogger(LogManager.getLogger(this.getClass()));
		
		super.getLogger().info("Import begins:");

		List<ImportRequest> request = new ArrayList<>();
		for(int i=1; i<args.length;i++) {
			try {
				if(args[i]!=null&&args[i].length()>0) {
					request.add(this.importRequest(args[i]));
					fileCount += 1;
				}
			} catch (Exception e) {
				super.getLogger().error("File '" + args[i] + "'", e);
				error=true;
			}
		}
		
		this.processImportRequestList(request);
		
		long globalEndTime = System.currentTimeMillis();
		super.getLogger().info("Import ends: {} files imported in {}ms", fileCount, (globalEndTime - globalStartTime));
	}

	protected abstract ImportRequest importRequest(final String arg) throws Exception;
	
	protected String extractRegionName(final String filename) throws Exception {
		// Parse filename back into region name
		String[] tokens = filename.split("\\.");
		if(tokens.length<3) {
			this.error=true;
			throw new Exception("File name '" + filename + "' not valid, needs region name, timestamp and format");
		}
		
		StringBuffer sb = new StringBuffer();
		for(int i=0; i< tokens.length-2 ; i++) {
			if(i!=0) {
				sb.append(".");
			}
			sb.append(tokens[i]);
		}
		
		return sb.toString();
	}
	
	protected abstract void processImportRequestList(final List<ImportRequest> importRequest) throws Exception;

	protected boolean isError() {
		return this.error;
	}
	protected void setError(boolean error) {
		this.error = error;
	}

	public static ClientCache getClientCache() {
		return clientCache;
	}

}
