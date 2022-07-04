import {ESLBaseElement} from '@exadel/esl/modules/esl-base-element/core';
import {ESLMediaQuery} from '@exadel/esl/modules/esl-media-query/core/esl-media-query';
import {throttle} from '@exadel/esl/modules/esl-utils/async/throttle';
import {memoize} from '@exadel/esl/modules/esl-utils/decorators/memoize';
import {CSSClassUtils} from '@exadel/esl/modules/esl-utils/dom/class';
import {EventUtils} from '@exadel/esl/modules/esl-utils/dom/events/utils';
import {AnchorScroller} from './eak-anchor-scroller';

import type {ESLToggleable} from '@exadel/esl/modules/esl-toggleable/core/esl-toggleable';

export class EAKAnchorNav extends ESLBaseElement {
  static is = 'eak-anchor-nav';

  public static BGCOLOR = '3, 226, 179';

  public static breakpoints = ESLMediaQuery.for('@MD').matches || ESLMediaQuery.for('@LG').matches || ESLMediaQuery.for('@XL').matches;

  protected $wrapper: HTMLElement;
  protected $anchorsContainer: HTMLElement;
  protected $dropdownTrigger: HTMLElement;
  protected $anchors: HTMLElement[];
  protected $targetsParams: any[];
  protected isDropdownMode: boolean;

  @memoize()
  get anchorsWidth(): number {
    return Array.from(this.$anchors).reduce((sum: number, anchor: HTMLElement, index: number, arr: []) => {
      return sum + parseInt(getComputedStyle(anchor).width) + (index < (arr.length - 1) ? 15 : 0);
    }, 0);
  }

  get containerWidth(): number {
    return parseInt(getComputedStyle(this.$wrapper).width);
  }

  get offset(): number {
    const headerHeight = (document.querySelector('.header') as HTMLElement)?.offsetHeight;
    const anchorNavigationHeight = (document.querySelector('main .eak-anchor-nav') as HTMLElement)?.offsetHeight;
    return headerHeight + (ESLMediaQuery.for('@MD').matches ? anchorNavigationHeight : 0);
  }

  @memoize()
  public get $scrollableContent(): HTMLElement | null {
    return document.querySelector('main.esl-scrollable-content');
  }

  get documentHeight(): number {
    return this.$scrollableContent?.scrollHeight || document.documentElement.clientHeight;
  }

  protected connectedCallback(): void {
    super.connectedCallback();
    this.$wrapper = this.querySelector('.anchor-nav-wrapper') as HTMLElement;
    this.$anchorsContainer = this.querySelector('.anchors-container') as HTMLElement;
    this.$dropdownTrigger = this.querySelector('.dropdown-trigger') as HTMLElement;
    this.$anchors = Array.from(this.querySelectorAll('.anchor-item'));

    new AnchorScroller();
    window.addEventListener('resize', throttle(this.onResize.bind(this), 500));
    this.addEventListener('click', (e) => this.onClick(e));
    window.addEventListener('scroll', throttle(this.highlightAnchors.bind(this), 500), true);

    this.onResize();
  }

  protected cleanAnchorHref($anchor: HTMLElement): string | undefined {
    return $anchor.getAttribute('href')?.slice(1);
  }

  protected onResize(): void {
    console.log('start', ESLMediaQuery.for('@MD').matches);
    EAKAnchorNav.breakpoints ? this.checkMainAnchorNavView() : this.checkHeaderAnchorNavView();
    this.checkNavigationMode();
    this.highlightAnchors();
  }

  protected checkMainAnchorNavView() {
    if (this.$scrollableContent && !this.$scrollableContent.contains(this)) {
      document.querySelector('main.esl-scrollable-content h2')?.after(this);
    }
  }

  protected checkNavigationMode(): void {
    this.isDropdownMode = !EAKAnchorNav.breakpoints || this.anchorsWidth > this.containerWidth;
    CSSClassUtils.toggle(this.$wrapper, 'dropdown-mode', this.isDropdownMode);
    CSSClassUtils.toggle(this.$wrapper, 'header-dropdown-mode', !EAKAnchorNav.breakpoints);
    this.anchorsWidth > this.containerWidth && this.setDropdownTitle();
  }

  protected checkHeaderAnchorNavView(): void  {
    const parent = document.querySelector('header .header-utility');
    if (parent && !parent.contains(this)) {
      parent.prepend(this);
    }
  }

  protected targetsParams() {
    this.$targetsParams = [];
    for(let i = 0; i < this.$anchors.length; i++) {
      const anchorId = this.cleanAnchorHref(this.$anchors[i]);
      if (anchorId) {
        const top = document.getElementById(anchorId)?.offsetTop || 0;
        const nextId = this.$anchors[i + 1] ? this.cleanAnchorHref(this.$anchors[i + 1]) : null;
        const next = nextId ? document.getElementById(nextId) : null;
        const height = (next ? next.offsetTop : this.documentHeight) - top;
        this.$targetsParams.push({
          anchorId,
          top,
          height,
          bottom: top + height
        })
      }
    }
  }

  protected highlightAnchors(): void {
    const windowHeight = document.documentElement.clientHeight - this.offset;
    const scrollTop = (this.$scrollableContent?.scrollTop || 0) + this.offset;
    const scrollBottom = scrollTop + windowHeight;

    this.targetsParams();
    this.$anchors.forEach((anchor) => {
      const indicator = anchor.firstElementChild as HTMLElement;
      indicator.style.opacity = '0';
      anchor.style.background = '';

      const targetParams = this.$targetsParams.find((target) => target.anchorId === this.cleanAnchorHref(anchor));
      if (targetParams.top > scrollBottom || targetParams.bottom < scrollTop) return;
      const visibleHeight = Math.max(targetParams.top, scrollTop) - Math.min(targetParams.bottom, scrollBottom);
      const normalisedPercentage = (Math.abs(visibleHeight/targetParams.height + visibleHeight/windowHeight) / 2).toFixed(1);

      this.isDropdownMode ? anchor.style.backgroundColor = `rgba(${EAKAnchorNav.BGCOLOR}, ${normalisedPercentage})` : indicator.style.opacity = `${normalisedPercentage}`;
    })
    this.anchorsWidth > this.containerWidth && this.setDropdownTitle();
   }


   protected onClick(e: Event): void {
    if (e.target instanceof HTMLAnchorElement) {
      e.preventDefault();
      this.isDropdownMode && (this.$anchorsContainer as ESLToggleable).hide();
      EventUtils.dispatch(window, 'hashchange:request', {
        bubbles: false,
        detail: {
          hash: e.target.getAttribute('href')
        }
      });
      this.isDropdownMode && this.setDropdownTitle();
    }
  }


  protected setDropdownTitle(): void {
  //  const id = new URL(location.href).hash.slice(1);
  //  console.log(document.getElementById(id)?.innerHTML);
    // if (id && this.$dropdownTrigger) {
    //   this.$dropdownTrigger.innerHTML = document.getElementById(id)?.innerHTML;
    // }
    // const $dropdownTitle = this.$dropdownTrigger.find(this.options.dropdownTitle);
    // const newTitle = item.find(this.options.itemTitle).text();
    // const currentTitle = $dropdownTitle.text();

    // if (newTitle === currentTitle) return;

    // $dropdownTitle.text(newTitle);
  }

  protected disconnectedCallback(): void {
    super.disconnectedCallback();
    window.removeEventListener('resize', this.checkNavigationMode.bind(this));
    this.removeEventListener('click', this.onClick);
    window.removeEventListener('scroll',  throttle(this.highlightAnchors.bind(this), 500), true);
  }

}
