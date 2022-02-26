import {ESLBaseElement, attr} from '@exadel/esl/modules/esl-base-element/core';
import {ready} from '@exadel/esl/modules/esl-utils/decorators/ready';
import {isIE} from '@exadel/esl/modules/esl-utils/environment/device-detector';
import {bind} from '@exadel/esl/modules/esl-utils/decorators/bind';
import {memoize} from '@exadel/esl/modules/esl-utils/decorators/memoize';
import {range} from '@exadel/esl/modules/esl-utils/misc/array';

export class EAKBannerLogo extends ESLBaseElement {
  static is = 'eak-banner-logo';

  static STARS_SEL = [
    '#eak-banner-stars-green > path',
    '#eak-banner-stars-white > path'
  ].join(',');

  @attr({defaultValue: '4'}) public targetsNumber: string;
  @attr({defaultValue: '3000'}) public iterationTime: string;

  private _$active: HTMLElement[] = [];
  private _animateTimer: number = 0;

  @ready
  protected connectedCallback(): void {
    super.connectedCallback();
    if (isIE) return;
    this.startAnimation();
  }
  protected disconnectedCallback(): void {
    this.stopAnimation();
    super.disconnectedCallback();
  }

  @memoize()
  public get $stars(): HTMLElement[] {
    return Array.from(document.querySelectorAll(EAKBannerLogo.STARS_SEL));
  }
  public get $randomStar(): HTMLElement {
    const index = Math.floor(Math.random() * this.$stars.length);
    return this.$stars[index];
  }

  public startAnimation(): void {
    memoize.clear(this, '$stars');
    this.stopAnimation();
    if (this.$stars.length < 2) return;
    this._animateTimer = window.setTimeout(this._onIteration, +this.iterationTime);
  }
  public stopAnimation(): void {
    this._animateTimer && window.clearTimeout(this._animateTimer);
  }

  @bind
  protected _onIteration(): void {
    const $candidates = range(+this.targetsNumber, () => this.$randomStar);
    this._$active.forEach((star) => star.classList.remove('animate'));
    $candidates.forEach((star) => star.classList.add('animate'));
    this._$active = $candidates;

    this._animateTimer = window.setTimeout(this._onIteration, +this.iterationTime);
  }
}
