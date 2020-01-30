(function () {

    function onSubmitForm(event) {

        let passwordForm = event.target;
        let passwordInput = passwordForm.querySelector(".secret-info-password");
        let passwordError = passwordForm.querySelector(".secret-info-error");

        if (passwordInput.value === passwordInput.dataset.password) {
            passwordForm.style.display = "none";
            passwordForm.parentElement.querySelector(".secret-info-text-wrap").style.display = "";
            passwordError.hidden = true;

        } else {
            passwordError.hidden = false;
        }

        event.preventDefault();
    }

    function onCloseText(event) {
        if (event.target.classList.contains("secret-info-text-hide")) {

            event.target.closest(".secret-info-text-wrap").style.display = "none";

            let passwordForm = document.body.querySelector(".secret-info-password-form");
            passwordForm.style.display = "";
            passwordForm.querySelector(".secret-info-password").value = "";
            passwordForm.querySelector(".secret-info-error").hidden = true;
        }
    }

    function bind() {
        document.body.addEventListener("submit", onSubmitForm);
        document.body.addEventListener("click", onCloseText);
    }

    bind();
})();