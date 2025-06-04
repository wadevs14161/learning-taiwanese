let score = Number(localStorage.getItem('score')) || 0;

document.addEventListener("DOMContentLoaded", function () {
    const scoreDisplay = document.querySelector('.score-display span');
    scoreDisplay.textContent = score;

    document.querySelectorAll('.option-button').forEach(btn => {
        btn.addEventListener('click', function () {
            if (btn.getAttribute('data-correct') === 'true') {
                score++;
                localStorage.setItem('score', score);
                scoreDisplay.textContent = score;
            } else {
                btn.classList.add('btn-danger');
                setTimeout(() => btn.classList.remove('btn-danger'), 500);
            }
            // Wait a moment, then load a new question (reload page)
            setTimeout(() => {
                window.location.reload();
            }, 700);
        });
    });
});