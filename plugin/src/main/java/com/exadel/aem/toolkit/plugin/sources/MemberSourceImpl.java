/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
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
package com.exadel.aem.toolkit.plugin.sources;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.main.Setting;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.handlers.EmbeddedMemberSource;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.plugin.metadata.Metadata;
import com.exadel.aem.toolkit.plugin.metadata.scripting.DataStack;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.MemberUtil;

/**
 * Presents an abstract implementation of {@link Source} that exposes the metadata that is specific for the underlying
 * class member
 */
class MemberSourceImpl extends SourceImpl implements ModifiableMemberSource {

    private final Class<?> componentType;

    private final Member member;
    private Class<?> declaringClass;
    private Class<?> reportingClass;
    private MemberSource upstreamSource;

    private String name;

    /**
     * Initializes a {@link Source} instance referencing the managed Java class member
     * @param member Reference to the class member
     */
    MemberSourceImpl(Member member) {
        super((AnnotatedElement) member);
        this.member = member;
        name = member.getName();
        declaringClass = member.getDeclaringClass();
        reportingClass = member.getDeclaringClass();
        componentType = MemberUtil.getComponentType(member);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String value) {
        name = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDeclaringClass(Class<?> value) {
        declaringClass = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getReportingClass() {
        return this.reportingClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReportingClass(Class<?> value) {
        this.reportingClass = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberSource getUpstreamSource() {
        return this.upstreamSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUpstreamSource(MemberSource value) {
        this.upstreamSource = value;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    // The usage of {@code Multifield#field} is retained for compatibility and will be removed after 2.0.2
    @Override
    public Class<?> getValueType() {
        // Retrieve the "immediate" return type
        Class<?> result = componentType;
        // Then switch to the directly specified type, if any
        if (adaptTo(MultiField.class) != null
            && adaptTo(MultiField.class).value() != _Default.class) {
            result = adaptTo(MultiField.class).value();
        } else if (adaptTo(MultiField.class) != null
            && adaptTo(MultiField.class).field() != _Default.class) {
            result = adaptTo(MultiField.class).field();
        } else if (adaptTo(FieldSet.class) != null
            && adaptTo(FieldSet.class).value() != _Default.class) {
            result = adaptTo(FieldSet.class).value();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSame(Source other) {
        if (!(other instanceof MemberSource)) {
            return super.isSame(other);
        }
        return getDeclaringClass().equals(((MemberSource) other).getDeclaringClass())
            && StringUtils.equals(getName(), other.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return member != null
            && !(member instanceof Field && member.getDeclaringClass().isInterface())
            && !Modifier.isStatic(member.getModifiers())
            && isWidgetAnnotationPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T adaptTo(Class<T> type) {
        boolean canCastToField = member instanceof Field && (type.equals(Field.class) || type.equals(Member.class));
        boolean canCastToMethod = member instanceof Method && (type.equals(Method.class) || type.equals(Member.class));
        if (canCastToField || canCastToMethod) {
            return type.cast(member);
        }
        return super.adaptTo(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    <T extends Annotation> T getAnnotation(Class<T> type) {
        return ((AnnotatedElement) member).getDeclaredAnnotation(type);
    }

    /**
     * {@inheritDoc} This implementation considers the {@link Setting} entries attached:
     * <br>- to the current field/method;
     * <br>- to the class where the current member is defined and all its superclasses/interfaces;
     * <br>- to the "upstream" member of the related class that triggered rendering of the current class;
     * <br>- and then to all the ancestors of that "upstream" class
     * @see Sources#fromMember(Member, Class, MemberSource)
     */
    @Override
    DataStack getDataStack() {
        DataStack result = new DataStack();

        // Collect the vector of upstreams for the current sources
        LinkedList<MemberSource> upstreamSources = new LinkedList<>();
        MemberSource currentUpstreamSource = upstreamSource;
        while (currentUpstreamSource instanceof EmbeddedMemberSource) {
            upstreamSources.add(currentUpstreamSource);
            currentUpstreamSource = ((EmbeddedMemberSource) currentUpstreamSource).getUpstreamSource();
        }

        // Add to the stack the settings stored at the class level in the declaring classes of upstream members
        // (less significant)
        upstreamSources.descendingIterator().forEachRemaining(upstream -> {
            for (Class<?> ancestor : ClassUtil.getInheritanceTree(upstream.getDeclaringClass())) {
                result.append(ancestor.getAnnotationsByType(Setting.class));
            }
        });

        // Add to the stack the settings stored at the class level in the declaring class of the current member and all
        // the parent classes of the declaring class
        for (Class<?> ancestor : ClassUtil.getInheritanceTree(getDeclaringClass())) {
            result.append(ancestor.getAnnotationsByType(Setting.class));
        }

        // Add to the stack the settings stored at the member level in all the upstream members
        upstreamSources.descendingIterator().forEachRemaining(upstream -> {
            result.append(upstream.adaptTo(Setting[].class));
        });

        // Add to the stack the settings stored at the member level locally (most significant)
        result.append(adaptTo(Setting[].class));
        return result;
    }

    /**
     * Gets whether the current class member has a widget annotation - the one with {@code sling:resourceType}
     * specified
     * @return True or false
     */
    private boolean isWidgetAnnotationPresent() {
        return Arrays.stream(adaptTo(Annotation[].class))
            .anyMatch(annotation -> {
                Metadata metadata = Metadata.from(annotation);
                ResourceType resourceType = metadata.getAnnotation(ResourceType.class);
                return resourceType != null && StringUtils.isNotBlank(resourceType.value());
            });
    }
}
