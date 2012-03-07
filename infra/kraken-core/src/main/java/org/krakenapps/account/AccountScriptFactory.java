/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.account;

import java.util.Properties;

import org.krakenapps.api.AccountManager;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.auth.api.AuthProvider;
import org.krakenapps.confdb.ConfigService;
import org.osgi.framework.BundleContext;

public class AccountScriptFactory implements ScriptFactory {
	private AccountManager manager;

	public AccountScriptFactory(BundleContext bc, ConfigService conf) {
		manager = new AccountManagerImpl(conf);
		bc.registerService(AuthProvider.class.getName(), manager, new Properties());
		bc.registerService(AccountManager.class.getName(), manager, new Properties());
	}

	@Override
	public Script createScript() {
		return new AccountScript(manager);
	}
}