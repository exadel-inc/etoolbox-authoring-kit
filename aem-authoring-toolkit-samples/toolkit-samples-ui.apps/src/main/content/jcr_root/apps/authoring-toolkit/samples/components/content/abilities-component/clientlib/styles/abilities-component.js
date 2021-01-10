(function () {

    function elementsButtonEvent(event) {
        if (event.target.classList.contains('warrior-abilities-button')) {
            const component = event.target.closest('.warrior-ability');
            const elementsList = component.querySelector('.warrior-abilities-list');
            elementsList.hidden = !elementsList.hidden;
        }
    }

    function bind() {
        document.body.addEventListener("click", elementsButtonEvent);
    }

    bind();
})();