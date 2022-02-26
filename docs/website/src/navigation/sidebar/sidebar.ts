import {prop, bind, ready, ESLToggleable, ESLMediaQuery} from '@exadel/esl';
import {attr} from '@exadel/esl/modules/esl-base-element/core';

import type {ToggleableActionParams} from '@exadel/esl';

interface SidebarActionParams extends ToggleableActionParams {
  /** Change state without animation */
  immediate: boolean;
}

export class ESLDemoSidebar extends ESLToggleable {
  static is = 'esl-d-sidebar';

  @prop() public closeOnEsc = true;
  @prop() public closeOnOutsideAction = true;

  @prop() public submenus: string = '.sidebar-nav-secondary';
  @prop() public activeMenuAttr: string = 'data-open';

  @attr({name: 'animation'}) protected _animation: boolean;

  public get $submenus(): ESLToggleable[] {
    return Array.from(this.querySelectorAll(this.submenus));
  }

  @ready
  protected connectedCallback() {
    super.connectedCallback();
    ESLMediaQuery.for('@+MD').addListener(this.onBreakpointChange);
  }
  protected disconnectedCallback() {
    super.disconnectedCallback();
    ESLMediaQuery.for('@+MD').removeListener(this.onBreakpointChange);
  }

  protected setInitialState() {
    const isDesktop = ESLMediaQuery.for('@+MD').matches;
    const isStoredOpen = !localStorage.getItem('sidebar-collapsed');
    this.toggle(isDesktop && isStoredOpen, {force: true, initiator: 'init', immediate: true});
  }

  public collapseAll() {
    this.$submenus.forEach((menu) => menu.hide({activator: this}));
  }

  public expandActive(noCollapse: boolean = false) {
    this.$submenus
      .filter((menu) => menu.hasAttribute('data-open'))
      .forEach((menu) => menu.show({noCollapse, activator: this}));
  }

  protected updateA11y() {
    const targetEl = this.$a11yTarget;
    if (!targetEl) return;
    targetEl.setAttribute('aria-expanded', String(this.open));
  }

  hide(params?: ToggleableActionParams): ESLToggleable {
    if (ESLMediaQuery.for('@+MD').matches) return this;
    return super.hide(params);
  }

  protected onShow(params: SidebarActionParams) {
    this._animation = !params.immediate;
    super.onShow(params);
    this.expandActive(params.initiator === 'init');
  }
  protected onHide(params: SidebarActionParams) {
    this._animation = !params.immediate;
    super.onHide(params);
    this.collapseAll();
  }

  @bind
  protected onBreakpointChange() {
    const isDesktop = ESLMediaQuery.for('@+MD').matches;
    this.toggle(isDesktop, {force: true, initiator: 'bpchange', immediate: !isDesktop});
  }
}
