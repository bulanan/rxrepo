package com.slimgears.rxrepo.queries;

import com.slimgears.util.autovalue.annotations.AutoValuePrototype;
import com.slimgears.util.autovalue.annotations.UseAutoValueAnnotator;
import com.slimgears.util.autovalue.annotations.UseBuilderExtension;

@AutoValuePrototype
@UseBuilderExtension
@UseAutoValueAnnotator
public interface TestKeyPrototype {
    String name();
}
