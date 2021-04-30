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

/**
 * @author Alexey Stsefanovich (ala'n)
 *
 * DependsOn Group Reference Registry.
 *
 * Stores and manages known group references
 * */
(function (document, $, ns) {
    'use strict';

    const GROUP_REF_SQ = ns.createSequence();
    class GroupReference extends ns.ObservedReference {
        constructor(name, $context) {
            super(`$group${GROUP_REF_SQ.next()}`);
            this.refs = [];
            this.name = name;
            this.$context = $context;
            this.onChange = this.update.bind(this);

            this.updateRefList();
        }

        /**
         * Destroy the reference
         * */
        remove() {
            super.remove();
            delete this.refs;
            delete this.$context;
        }

        /**
         * Get the current element's value
         * */
        getReferenceValue() {
            return (this.refs || []).map((ref) => ref.value);
        }

        /**
         * Update the child references list
         * */
        updateRefList() {
            this.refs.forEach((ref) => ref.unsubscribe(this.onChange));
            this.refs = ns.ElementReferenceRegistry.getAllByRefName(this.name, this.$context);
            this.refs.forEach((ref) => ref.subscribe(this.onChange));
        }

        /**
         * Check if the group reference matches the provided definition
         * @param {string} name
         * @param {jQuery | HTMLElement | string} [$context]
         * @returns {boolean}
         * */
        is(name, $context) {
            return name === this.name && this.$context.is($context);
        }

        /**
         * Check if the group reference has listeners and an actual context
         * @returns {boolean}
         * */
        isOutdated() {
            return !this.listenersCount || !this.$context.closest('html').length;
        }
    }

    let refs = [];
    class GroupReferenceRegistry {
        /**
         * Register {GroupReference}.
         * Return the existing one if it is already registered
         * */
        static register(name, $context) {
            for (const ref of refs) {
                if (ref.is(name, $context)) return ref;
            }
            const newRef = new GroupReference(name, $context);
            refs.push(newRef);
            return newRef;
        }

        /**
         * Return all known group references.
         * @returns {Array<GroupReference>}
         * */
        static get refs() { return refs; }

        /**
         * Remove the outdated references and update the actual ones
         * */
        static actualize() {
            refs = refs.filter((ref) => {
                if (ref.isOutdated()) {
                    ref.remove();
                    return false;
                }
                ref.updateRefList();
                ref.update();
                return true;
            });
        }
    }
    ns.GroupReferenceRegistry = GroupReferenceRegistry;
})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
