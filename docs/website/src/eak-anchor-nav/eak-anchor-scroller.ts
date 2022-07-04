import {scrollIntoView} from '@exadel/esl/modules/esl-utils/dom/scroll';

let instance: AnchorScroller;

export class AnchorScroller {

  public static get hasHash(): boolean {
    return location.href.indexOf('#') !== -1;
  }

  public static cleanHash(hash: string): string {
    return hash.trim().replace(/^#?/, '');
  }

  public static get targetAnchor(): Element | null {
    return document.getElementById(AnchorScroller.cleanHash(location.hash));
  }

  constructor() {
    if (instance) return instance;
    window.addEventListener('load', this.handleScroll.bind(this), {once: true});
    window.addEventListener('hashchange:request', (e) => this.onHashChange.bind(this)(e));
    window.addEventListener('hashchange', (e) => this.onHashChange.bind(this)(e));
    instance = this;
  }

  protected onHashChange(e: CustomEvent) {
    const hash = AnchorScroller.cleanHash(e.detail.hash);
    history.pushState(null, '', `#${hash}`);
    hash && this.handleScroll();
  }

  protected handleScroll(): void {
    if (!AnchorScroller.hasHash) return;

    AnchorScroller.targetAnchor &&
    scrollIntoView(AnchorScroller.targetAnchor, {behavior: 'smooth', block: 'center'})
  }
}
