import { attr, ESLBaseElement, listen } from "@exadel/esl/modules/esl-base-element/core";

export class MdImage extends ESLBaseElement {

    static is = "md-image";

    @attr({ dataAttr: true })
    public src: string
    private isZoomActive: boolean = false

    connectedCallback() {
        super.connectedCallback()

        const originalImage = document.createElement('esl-image');
        originalImage.setAttribute("data-src", this.src)
        originalImage.setAttribute("lazy", '')
        originalImage.setAttribute("mode", 'fit')


        this.appendChild(originalImage)

    }

    disconnectedCallback() {
        super.disconnectedCallback()
        // cleanup 
    }

    @listen('click')
    onClick() {
        this.isZoomActive = !this.isZoomActive
        this.isZoomActive ? this.classList.add('md-image-zoom') : this.classList.remove('md-image-zoom')
    };


    @listen('mousemove')
    onMouseMove(e: MouseEvent) {

        if (this.isZoomActive) {

            let img = this.firstChild?.firstChild  // esl-image > img


            img.style.right = e.clientX - this.offsetWidth + 'px'
            img.style.bottom = e.clientY - this.offsetHeight + 'px'

        }
    }

}

