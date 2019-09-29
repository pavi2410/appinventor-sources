// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate Android permissions required by components.
 *
 * @author markf@google.com (Mark Friedman)
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface UsesPermissions {

  /**
   * The names of the permissions separated by commas.
   *
   * @return  the permission name
   * @see android.Manifest.permission
   */
  String permissionNames() default "";

  /**
   * The names of the permissions as a list.
   *
   * @return  the permission names
   * @see android.Manifest.permission
   */
  String[] value() default {};
}
