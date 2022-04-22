import {
  attr,
  ESLBaseElement,
  listen,
} from '@exadel/esl/modules/esl-base-element/core';
import { DeviceDetector } from '@exadel/esl';

export class MdImage extends ESLBaseElement {
  static is = 'md-image';

  @attr({ dataAttr: true })
  public src: string;

  static zoomClass: string = 'md-image-zoom';

  connectedCallback(): void {
    super.connectedCallback();

    const originalImage = document.createElement('esl-image');
    originalImage.setAttribute('data-src', this.src);
    originalImage.setAttribute('lazy', '');
    originalImage.setAttribute('mode', 'fit');

    this.appendChild(originalImage);
  }

  @listen('click')
  protected onClick(): void {
    this.classList.toggle(MdImage.zoomClass);

    if (!this.classList.contains(MdImage.zoomClass)) {
      this.$$off('touchmove mousemove');
      return;
    }

    if (DeviceDetector.isMobile) {
      this.$$on('touchmove', this.onTouchMove);
    } else {
      this.$$on('mousemove', this.onMouseMove);
    }
  }

  protected getImg(): HTMLImageElement | null {
    return this.querySelector('.inner-image') as HTMLImageElement; // esl-image > img
  }

  protected onMouseMove(e: MouseEvent): void {
    const img = this.getImg();

    if (!img) return;

    img.style.right = e.clientX - this.offsetWidth + 'px';
    img.style.bottom = e.clientY - this.offsetHeight + 'px';
  }

  protected onTouchMove(e: TouchEvent): void {
    const img = this.getImg();

    if (!img) return;

    img.style.right = e.changedTouches[0].clientX - this.clientWidth + 'px';
    img.style.bottom =
      e.changedTouches[0].clientY / 2 - this.clientHeight + 'px';
  }
}
