let ingredientIndex = 1;

document.getElementById('add-ingredient-btn').addEventListener('click', function () {
    const container = document.getElementById('ingredient-list');

    const div = document.createElement('div');
    div.className = 'ingredient-group';

    div.innerHTML = `
        <input type="text" name="ingredients[${ingredientIndex}].name" placeholder="e.g. Sugar" required />
        <input type="text" name="ingredients[${ingredientIndex}].unit" placeholder="e.g. tbsp" required />
        <input type="number" step="0.01" min="0" name="ingredients[${ingredientIndex}].amount" placeholder="e.g. 50" required />
        <button type="button" class="remove-ingredient-btn">–</button>
    `;

    container.appendChild(div);
    ingredientIndex++;
});

document.getElementById('ingredient-list').addEventListener('click', function (e) {
    if (e.target.classList.contains('remove-ingredient-btn')) {
        const group = e.target.closest('.ingredient-group');
        group.remove();
    }
});
