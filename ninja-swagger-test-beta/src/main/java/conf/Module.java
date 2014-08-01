/*
 * Copyright 2014 gitblit.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package conf;

import ninja.UsernamePasswordValidator;
import ninja.params.ParamParsers;
import ninja.swagger.SwaggerModule;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import etc.OrderStatus;
import etc.PetStatus;

/**
 * Module references all the core business objects.
 *
 * @author James Moger
 *
 */
@Singleton
public class Module extends AbstractModule {

	@Override
	protected void configure() {

		install(new SwaggerModule());

		// bind our demo credentials validator
		bind(UsernamePasswordValidator.class).toInstance(new UsernamePasswordValidator() {

			@Override
			public boolean validateCredentials(String username, String password) {
				return "demo".equals(username) && "demo".equals(password);
			}
		});

		// Register our enums
		ParamParsers.registerEnum(PetStatus.class);
		ParamParsers.registerEnum(OrderStatus.class);

	}
}
