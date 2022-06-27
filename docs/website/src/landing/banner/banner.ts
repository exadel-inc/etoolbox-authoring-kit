import { ESLBaseElement, attr } from '@exadel/esl/modules/esl-base-element/core';
import { ready } from '@exadel/esl/modules/esl-utils/decorators/ready';
import { isIE } from '@exadel/esl/modules/esl-utils/environment/device-detector';
import { bind } from '@exadel/esl/modules/esl-utils/decorators/bind';
import { memoize } from '@exadel/esl/modules/esl-utils/decorators/memoize';
import { range } from '@exadel/esl/modules/esl-utils/misc/array';

export class EAKBanner extends ESLBaseElement {
  static is = 'eak-banner';

  static TARGETS = 'g > path';

  @attr({ defaultValue: '5' }) public targetsNumber: string;
  @attr({ defaultValue: '2500' }) public iterationTime: string;

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
  public get $lines(): SVGGeometryElement[] {
    return Array.from(document.querySelectorAll(EAKBanner.TARGETS));
  }
  public get $randomLine(): SVGGeometryElement {
    const index = Math.floor(Math.random() * this.$lines.length);
    return this.$lines[index];
  }

  public startAnimation(): void {
    memoize.clear(this, '$lines');
    this.stopAnimation();
    if (this.$lines.length < 2) return;
    this._animateTimer = window.setTimeout(this._onIteration, +this.iterationTime);
  }
  public stopAnimation(): void {
    this._animateTimer && window.clearTimeout(this._animateTimer);
  }

  @bind
  protected _onIteration(): void {
    const $candidates = range(+this.targetsNumber, () => this.$randomLine);

    $candidates.forEach((line: SVGGeometryElement) => {
      let lineLength = line.getTotalLength();
      line.classList.add('animate');

      line.style.strokeDasharray = lineLength + ' ';
      line.style.strokeDashoffset = -lineLength + '';
    });

    this._animateTimer = window.setTimeout(this._onIteration, +this.iterationTime);
  }
}
