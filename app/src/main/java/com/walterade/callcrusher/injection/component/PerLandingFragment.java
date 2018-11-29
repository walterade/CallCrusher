package com.walterade.callcrusher.injection.component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by Walter on 10/11/17.
 */

@Scope
@Retention(RetentionPolicy.RUNTIME) public @interface PerLandingFragment {}