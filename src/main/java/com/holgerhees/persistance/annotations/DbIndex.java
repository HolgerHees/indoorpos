package com.holgerhees.persistance.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DbIndex
{
	enum Type
	{
		PRIMARY_KEY
		{
			@Override
			public String getSqlStatement(String... columnNames)
			{
				return "PRIMARY KEY (`" + split(columnNames, "`, `") + "`)";
			}
		},

		UNIQUE
		{
			@Override
			public String getSqlStatement(String... columnNames)
			{
				return "UNIQUE KEY " + getName( this, columnNames ) + " (`" + split(columnNames, "`, `") + "`)";
			}
		},

		INDEX
		{
			@Override
			public String getSqlStatement(String... columnNames)
			{
				return "KEY " + getName( this, columnNames ) + " (`" + split(columnNames, "`, `") + "`)";
			}
		},

		FULLTEXT
		{
			@Override
			public String getSqlStatement(String... columnNames)
			{
				return "FULLTEXT (`" + split(columnNames, "`, `") + "`)";
			}
		},

		DESC_INDEX
		{
			@Override
			public String getSqlStatement(String... columnNames)
			{
				return "KEY " + getName( this, columnNames ) + " (`" + split(columnNames, "`, `") + "` DESC)";
			}
		};

		public abstract String getSqlStatement(String... columnNames);
		
		public static String getName(DbIndex.Type type, String... columnNames){
			if( type.equals(DbIndex.Type.PRIMARY_KEY ) )
			{
				return "PRIMARY";
			}
			else if( type.equals(DbIndex.Type.UNIQUE ) )
			{
				return split(columnNames, "_") + "_unique";
			}
			else
			{
				return split(columnNames, "_") + "_key";
			}
		}

		private static String split(String[] array, String separator)
		{
			if (array.length == 0)
			{
				return "";
			}
			StringBuilder result = new StringBuilder();
			result.append(array[0]);
			for (int i = 1; i < array.length; i++)
			{
				result.append(separator).append(array[i]);
			}
			return result.toString();
		}
	}
	
	DbIndex DEFAULT = new DbIndex() {
		@Override
		public Class<? extends Annotation> annotationType() {
			return DbIndex.class;
		}
		
		@Override
		public Type type() {
			return DbIndex.Type.INDEX;
		}
		
		@Override
		public String group() {
			return "";
		}
	};

	Type type();
	String group() default "";
}