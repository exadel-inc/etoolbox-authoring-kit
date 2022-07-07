import {attr} from '@exadel/esl/modules/esl-utils/decorators/attr';
import {prop} from '@exadel/esl/modules/esl-utils/decorators/prop';
import {listen} from '@exadel/esl/modules/esl-utils/decorators/listen';
import {ESLMediaQuery} from '@exadel/esl/modules/esl-media-query/core';
import {ESLToggleable} from '@exadel/esl/modules/esl-toggleable/core';

import type {ToggleableActionParams} from '@exadel/esl/modules/esl-toggleable/core';
import type {ESLMediaChangeEvent} from '@exadel/esl/modules/esl-media-query/core/conditions/media-query-base';

interface SidebarActionParams extends ToggleableActionParams {
  /** Change state without animation */
  immediate: boolean;
}

export class EAKSidebar extends ESLToggleable {
  static is = 'eak-sidebar';

  @prop() public closeOnEsc = true;
  @prop() public closeOnOutsideAction = true;

  @prop() public submenus: string = '.sidebar-nav-secondary.nav-secondary-panel';
  @prop() public activeMenuAttr: string = 'data-open';

  @attr({name: 'animation'}) protected _animation: boolean;

  public get $submenus(): ESLToggleable[] {
    return Array.from(this.querySelectorAll(this.submenus));
  }

  protected setInitialState(): void {
    const isDesktop = ESLMediaQuery.for('@+MD').matches;
    this.toggle(isDesktop, {force: true, initiator: 'init', immediate: true});
  }

  public collapseAll(): void {
    this.$submenus.forEach((menu) => menu.hide({activator: this}));
  }

  public expandActive(noCollapse: boolean = false): void {
    this.$submenus
      .filter((menu) => menu.hasAttribute('data-open'))
      .forEach((menu) => menu.show({noCollapse, activator: this}));
  }

  protected updateA11y(): void {
    const targetEl = this.$a11yTarget;
    if (!targetEl) return;
    targetEl.setAttribute('aria-expanded', String(this.open));
  }

  hide(params?: ToggleableActionParams): ESLToggleable {
    if (ESLMediaQuery.for('@+MD').matches) return this;
    return super.hide(params);
  }

  protected onShow(params: SidebarActionParams): void {
    this._animation = !params.immediate;
    super.onShow(params);
    this.expandActive(params.initiator === 'init');
  }
  protected onHide(params: SidebarActionParams): void {
    this._animation = !params.immediate;
    super.onHide(params);
    this.collapseAll();
  }

  @listen({
    event: 'change',
    target: ESLMediaQuery.for('@+MD')
  })
  protected onBreakpointChange({matches: isDesktop}: ESLMediaChangeEvent): void {
    this.toggle(isDesktop, {force: true, initiator: 'bp-change', immediate: !isDesktop});
  }
}
