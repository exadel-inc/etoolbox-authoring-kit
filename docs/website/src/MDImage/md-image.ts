import { attr, boolAttr, ESLBaseElement, listen } from "@exadel/esl/modules/esl-base-element/core";

export class MdImage extends ESLBaseElement {

    static is = "md-image";

    @attr({ dataAttr: true })
    public src: string
    @boolAttr() private isZoomActive = false

    connectedCallback() {
        super.connectedCallback()

        const eslImage = document.createElement('esl-image');
        eslImage.setAttribute("data-src", this.src)
        eslImage.setAttribute("lazy", '')
        eslImage.setAttribute("mode", 'fit')


        this.appendChild(eslImage)
        this.addEventListener('click', this.onClick)
    }

    disconnectedCallback() {
        super.disconnectedCallback()
        // cleanup 

        this.removeEventListener('click', this.onClick)
    }

    @listen('click')
    onClick() {
        this.isZoomActive = !this.isZoomActive
        console.log("click")
    };

}

