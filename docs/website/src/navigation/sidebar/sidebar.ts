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

  protected storeState() {
    this.open ? localStorage.removeItem('sidebar-collapsed') : localStorage.setItem('sidebar-collapsed', 'true');
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

  protected onShow(params: SidebarActionParams) {
    this._animation = !params.immediate;
    super.onShow(params);
    this.expandActive(params.initiator === 'init');
    if (params.activator && params.activator.hasAttribute('data-store')) {
      this.storeState();
    }
  }
  protected onHide(params: SidebarActionParams) {
    this._animation = !params.immediate;
    super.onHide(params);
    this.collapseAll();
    if (params.activator && params.activator.hasAttribute('data-store')) {
      this.storeState();
    }
  }

  @bind
  protected onBreakpointChange() {
    const isDesktop = ESLMediaQuery.for('@+MD').matches;
    const isStoredOpen = !localStorage.getItem('sidebar-collapsed');
    this.toggle(isDesktop && isStoredOpen, {force: true, initiator: 'bpchange', immediate: !isDesktop});
  }

  @bind
  protected _onOutsideAction(e: MouseEvent) {
    if (ESLMediaQuery.for('@+MD').matches) return;
    super._onOutsideAction(e);
  }
}
