// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate asset files required by components.
 *
 * @author trevorbadams@gmail.com (Trevor Adams)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsesAssets {
  /**
   * The filenames of the required assets separated by commas.
   * 
   * @deprecated use {@link #value()} instead
   */
  @Deprecated
  String fileNames() default "";
  
  /**
   * An array of the names of assets
   */
  String[] value() default {};
}
