// Verbatim Studio - Core Application Logic

// Initialize Theme Immediately
initTheme();

function isAndroid() {
    return typeof window.AndroidBridge !== 'undefined';
}

function initTheme() {
    const savedTheme = localStorage.getItem('cs_theme');
    if (savedTheme) {
        setTheme(savedTheme);
    } else {
        const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        setTheme(prefersDark ? 'dark' : 'light');
    }
}

function setTheme(theme) {
    const html = document.documentElement;
    const themeToggleBtn = document.getElementById('themeToggleBtn');
    const isDark = theme === 'dark';
    
    if (isDark) {
        html.classList.add('dark');
        if (themeToggleBtn) {
            const icon = themeToggleBtn.querySelector('i');
            if (icon) icon.className = "fa-solid fa-sun text-xs sm:text-sm";
            themeToggleBtn.setAttribute('title', 'Switch to Light Mode');
        }
        localStorage.setItem('cs_theme', 'dark');
    } else {
        html.classList.remove('dark');
        if (themeToggleBtn) {
            const icon = themeToggleBtn.querySelector('i');
            if (icon) icon.className = "fa-solid fa-moon text-xs sm:text-sm";
            themeToggleBtn.setAttribute('title', 'Switch to Dark Mode');
        }
        localStorage.setItem('cs_theme', 'light');
    }
}

function toggleTheme() {
    const isDark = document.documentElement.classList.contains('dark');
    setTheme(isDark ? 'light' : 'dark');
    if (isAndroid()) window.AndroidBridge.vibrateClick();
    showToast(isDark ? "Switched to Light Theme" : "Switched to Dark Theme");
}

// Elements
const input = document.getElementById('userInput');
const output = document.getElementById('mirrorOutput');
const charCount = document.getElementById('charCount');
const wordCount = document.getElementById('wordCount');
const readingTime = document.getElementById('readingTime');
const readabilityComplexity = document.getElementById('readabilityComplexity');
const loadingOverlay = document.getElementById('loadingOverlay');
const modeBadge = document.getElementById('modeBadge');
const currentProviderLabel = document.getElementById('currentProviderLabel');
const apiKeySection = document.getElementById('apiKeySection');
const apiKeyInput = document.getElementById('apiKeyInput');
const modelSelect = document.getElementById('modelSelect');
const testBtn = document.getElementById('testBtn');
const testStatus = document.getElementById('testStatus');
const historyModeSelect = document.getElementById('historyModeSelect');
const historyRetentionSelect = document.getElementById('historyRetentionSelect');
const customRetentionSection = document.getElementById('customRetentionSection');
const customRetentionInput = document.getElementById('customRetentionInput');
const incognitoToggleBtn = document.getElementById('incognitoToggleBtn');

let isAiMode = false;
let activeTab = 'input';
let rawInputText = "";
let rawOutputText = "";
let showDiff = false;
let history = [];

// Text to speech state
const synth = window.speechSynthesis;
let utterance = null;
let isSpeakingAudio = false;

window.addEventListener('DOMContentLoaded', () => {
    initTheme();
    loadSettings();
    loadHistory();
    loadCustomPresets();
    setupEventListeners();
});

function setupEventListeners() {
    if (input) {
        input.addEventListener('input', function () {
            const text = this.value;
            rawInputText = text;
            const chars = text.length;
            const words = text.trim() ? text.trim().split(/\s+/).length : 0;
            const minutes = Math.ceil(words / 200);
            
            if (charCount) charCount.textContent = `${chars} chars`;
            if (wordCount) wordCount.textContent = `${words} words`;
            if (readingTime) readingTime.textContent = `${minutes} min read`;
            
            let complexity = "Easy";
            if (words > 40) {
                const wordLength = text.replace(/\s+/g, '').length / words;
                if (wordLength > 5.8) complexity = "Advanced";
                else if (wordLength > 4.6) complexity = "Medium";
            }
            if (readabilityComplexity) readabilityComplexity.textContent = complexity;
            
            if (isAiMode) {
                isAiMode = false;
                if (modeBadge) modeBadge.classList.add('hidden');
            }
            
            if (chars === 0) {
                if (output) output.innerHTML = '<span class="text-slate-400 dark:text-slate-500 italic">Result will appear here...</span>';
                rawOutputText = "";
            } else {
                if (!rawOutputText || rawOutputText === "Result will appear here...") {
                    if (output) output.textContent = text;
                }
            }
        });
    }

    if (historyRetentionSelect) {
        historyRetentionSelect.addEventListener('change', (e) => {
            toggleCustomRetention(e.target.value);
        });
    }

    if (customRetentionInput) {
        customRetentionInput.addEventListener('input', () => {
            if (customRetentionInput.value.trim() && parseFloat(customRetentionInput.value.trim()) > 0) {
                customRetentionInput.classList.remove('border-red-500', 'ring-1', 'ring-red-500');
            }
        });
    }

    setupVisibilityToggle('apiKeyInput', 'toggleGeminiKey');

    if (testBtn) {
        testBtn.addEventListener('click', testConnection);
    }
}

// Diff Engine
function toggleDiff() {
    showDiff = !showDiff;
    const diffToggleBtn = document.getElementById('diffToggleBtn');
    if (isAndroid()) window.AndroidBridge.vibrateClick();
    
    if (showDiff) {
        if (diffToggleBtn) {
            diffToggleBtn.classList.remove('text-slate-500', 'dark:text-slate-400');
            diffToggleBtn.classList.add('bg-indigo-600', 'text-white');
        }
        showToast("Diff highlight active");
    } else {
        if (diffToggleBtn) {
            diffToggleBtn.classList.remove('bg-indigo-600', 'text-white');
            diffToggleBtn.classList.add('text-slate-500', 'dark:text-slate-400');
        }
        showToast("Diff highlight deactivated");
    }
    renderOutput();
}

function renderOutput() {
    if (!output) return;
    if (!rawOutputText || rawOutputText === "Result will appear here...") {
        output.innerHTML = '<span class="text-slate-400 dark:text-slate-500 italic">Result will appear here...</span>';
        return;
    }
    if (showDiff && rawInputText) {
        output.innerHTML = diffWords(rawInputText, rawOutputText);
    } else {
        output.textContent = rawOutputText;
    }
}

// Mobile Tab Switcher
function switchTab(tab) {
    activeTab = tab;
    const inputCard = document.getElementById('inputCard');
    const outputCard = document.getElementById('outputCard');
    const tabIndicator = document.getElementById('tabIndicator');
    const tabBtnInput = document.getElementById('tabBtnInput');
    const tabBtnOutput = document.getElementById('tabBtnOutput');

    if (isAndroid()) window.AndroidBridge.vibrateClick();

    if (tab === 'input') {
        if (inputCard) {
            inputCard.classList.remove('hidden');
            inputCard.classList.add('flex');
        }
        if (outputCard) {
            outputCard.classList.add('hidden');
            outputCard.classList.remove('flex');
        }
        if (tabIndicator) tabIndicator.style.transform = 'translateX(0)';
        if (tabBtnInput) {
            tabBtnInput.classList.remove('text-slate-500', 'dark:text-slate-400');
            tabBtnInput.classList.add('text-white');
        }
        if (tabBtnOutput) {
            tabBtnOutput.classList.remove('text-white');
            tabBtnOutput.classList.add('text-slate-500', 'dark:text-slate-400');
        }
    } else {
        if (outputCard) {
            outputCard.classList.remove('hidden');
            outputCard.classList.add('flex');
        }
        if (inputCard) {
            inputCard.classList.add('hidden');
            inputCard.classList.remove('flex');
        }
        if (tabIndicator) tabIndicator.style.transform = 'translateX(100%)';
        if (tabBtnOutput) {
            tabBtnOutput.classList.remove('text-slate-500', 'dark:text-slate-400');
            tabBtnOutput.classList.add('text-white');
        }
        if (tabBtnInput) {
            tabBtnInput.classList.remove('text-white');
            tabBtnInput.classList.add('text-slate-500', 'dark:text-slate-400');
        }
    }
}

// Trigger AI Transformation
async function triggerAI(mode) {
    const text = input ? input.value.trim() : '';
    if (!text) {
        showToast("Please write or paste some text first!", true);
        return;
    }

    if (isAndroid()) window.AndroidBridge.vibrateClick();

    if (loadingOverlay) {
        loadingOverlay.classList.remove('hidden');
        loadingOverlay.classList.add('flex');
        setTimeout(() => loadingOverlay.classList.remove('opacity-0'), 10);
    }

    isAiMode = true;
    if (window.innerWidth < 768) {
        switchTab('output');
    }

    try {
        const resultText = await callGemini(mode, text);
        rawOutputText = resultText;
        if (modeBadge) modeBadge.classList.remove('hidden');
        
        const customMatch = customPresets.find(p => p.id === mode);
        const builtInMatch = defaultPresets.find(p => p.id === mode);
        const displayModeName = customMatch ? customMatch.name : (builtInMatch ? builtInMatch.name : mode.toUpperCase());
        
        if (modeBadge) modeBadge.textContent = `Preset: ${displayModeName}`;
        renderOutput();
        saveHistoryEntry(displayModeName, text, resultText);
    } catch (error) {
        console.error(error);
        if (output) {
            output.innerHTML = `<span class="text-rose-600 dark:text-rose-400 font-medium"><i class="fa-solid fa-triangle-exclamation"></i> Error: ${escapeHtml(error.message)}</span>`;
        }
    } finally {
        if (loadingOverlay) {
            loadingOverlay.classList.add('opacity-0');
            setTimeout(() => {
                loadingOverlay.classList.add('hidden');
                loadingOverlay.classList.remove('flex');
            }, 200);
        }
    }
}

// Call Google Gemini API with fallback
async function callGemini(mode, text) {
    const apiKey = localStorage.getItem('cs_gemini_key');
    if (!apiKey) throw new Error("Missing Gemini API Key. Go to Settings.");

    const prompt = getPrompt(mode, text);
    const selectedModel = localStorage.getItem('cs_gemini_model') || 'gemini-3.5-flash-lite';
    const defaultModels = [
        "gemini-3.5-flash-lite",
        "gemini-3.1-flash-lite",
        "gemini-2.5-flash-lite",
        "gemini-3.6-flash",
        "gemini-3.5-flash",
        "gemini-3-flash-preview",
        "gemini-2.5-flash",
        "gemini-3.1-pro-preview",
        "gemini-2.5-pro"
    ];

    const models = [ selectedModel, ...defaultModels.filter(m => m !== selectedModel) ];
    const payload = {
        contents: [ { parts: [ { text: prompt } ] } ]
    };

    const sleep = (ms) => new Promise(res => setTimeout(res, ms));

    async function tryModel(model) {
        const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`;
        for (let attempt = 1; attempt <= 2; attempt++) {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 20000);
            try {
                const response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'x-goog-api-key': apiKey
                    },
                    body: JSON.stringify(payload),
                    signal: controller.signal
                });
                clearTimeout(timeoutId);

                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    const status = response.status;
                    const msg = errData.error?.message || `HTTP ${status}`;
                    const retryable = status === 429 || status >= 500;
                    if (attempt === 1 && retryable) {
                        await sleep(1000 * attempt);
                        continue;
                    }
                    throw new Error(`${model} failed (${status}): ${msg}`);
                }

                const data = await response.json();
                const resultText = data?.candidates?.[ 0 ]?.content?.parts?.[ 0 ]?.text;
                if (!resultText) throw new Error("Empty response payload");
                return resultText;
            } catch (err) {
                clearTimeout(timeoutId);
                const isLastAttempt = attempt === 2;
                if (!isLastAttempt) {
                    await sleep(1000 * attempt);
                    continue;
                }
                throw err;
            }
        }
    }

    let lastError;
    for (const model of models) {
        try {
            return await tryModel(model);
        } catch (err) {
            console.warn(`Fallback triggered: ${model} failed`, err.message);
            lastError = err;
        }
    }
    throw lastError || new Error("All Gemini models failed after retries");
}

// Built-in Presets
const defaultPresets = [
    { id: 'improve', name: 'Correct & Polish', icon: 'fa-wand-magic-sparkles', class: 'bg-indigo-50 dark:bg-indigo-500/10 border-indigo-100 dark:border-indigo-500/20 text-indigo-600 dark:text-indigo-300 hover:bg-indigo-600 hover:text-white' },
    { id: 'professional', name: 'Professional', icon: 'fa-briefcase', class: 'bg-emerald-50 dark:bg-emerald-500/10 border-emerald-100 dark:border-emerald-500/20 text-emerald-600 dark:text-emerald-300 hover:bg-emerald-600 hover:text-white' },
    { id: 'casual', name: 'Conversational', icon: 'fa-comments', class: 'bg-sky-50 dark:bg-sky-500/10 border-sky-100 dark:border-sky-500/20 text-sky-600 dark:text-sky-300 hover:bg-sky-600 hover:text-white' },
    { id: 'summarize', name: 'Summarize', icon: 'fa-compress', class: 'bg-amber-50 dark:bg-amber-500/10 border-amber-100 dark:border-amber-500/20 text-amber-600 dark:text-amber-300 hover:bg-amber-600 hover:text-white' },
    { id: 'bullet', name: 'Bullet Points', icon: 'fa-list-ul', class: 'bg-purple-50 dark:bg-purple-500/10 border-purple-100 dark:border-purple-500/20 text-purple-600 dark:text-purple-300 hover:bg-purple-600 hover:text-white' },
    { id: 'expand', name: 'Expand', icon: 'fa-expand', class: 'bg-cyan-50 dark:bg-cyan-500/10 border-cyan-100 dark:border-cyan-500/20 text-cyan-600 dark:text-cyan-300 hover:bg-cyan-600 hover:text-white' },
    { id: 'sarcastic', name: 'Sarcastic', icon: 'fa-mask', class: 'bg-rose-50 dark:bg-rose-500/10 border-rose-100 dark:border-rose-500/20 text-rose-600 dark:text-rose-300 hover:bg-rose-600 hover:text-white' },
    { id: 'prompt', name: 'Prompt Architect', icon: 'fa-robot', class: 'bg-orange-50 dark:bg-orange-500/10 border-orange-100 dark:border-orange-500/20 text-orange-600 dark:text-orange-300 hover:bg-orange-600 hover:text-white' }
];

let customPresets = [];
const STRICT_FORMATTING_SUFFIX = "\n\nCRITICAL INSTRUCTION: Return ONLY the processed result as plain text. Do NOT include any introductory greetings, commentary, labels, quotes, explanations, or markdown code block formatting (unless raw markdown is explicitly requested).";

function loadCustomPresets() {
    const saved = localStorage.getItem('cs_custom_presets');
    if (saved) {
        try {
            customPresets = JSON.parse(saved);
        } catch (e) {
            customPresets = [];
        }
    } else {
        customPresets = [];
    }
    renderPresetCarousel();
    renderCustomPresetsList();
    setupCarouselScroll();
    setupPresetFormKeyboardShortcuts();
}

function setupPresetFormKeyboardShortcuts() {
    const promptInput = document.getElementById('newPresetPrompt');
    const nameInput = document.getElementById('newPresetName');
    if (promptInput && !promptInput.dataset.shortcutBound) {
        promptInput.dataset.shortcutBound = "true";
        promptInput.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                e.preventDefault();
                addOrUpdateCustomPreset();
            }
        });
    }
    if (nameInput && !nameInput.dataset.shortcutBound) {
        nameInput.dataset.shortcutBound = "true";
        nameInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                if (promptInput) promptInput.focus();
            }
        });
    }
}

function scrollPresetCarousel(offset) {
    const carousel = document.getElementById('tonePresetsCarousel');
    if (carousel) {
        carousel.scrollBy({ left: offset, behavior: 'smooth' });
    }
}

function setupCarouselScroll() {
    const carousel = document.getElementById('tonePresetsCarousel');
    if (!carousel || carousel.dataset.scrollBound) return;
    carousel.dataset.scrollBound = "true";

    carousel.addEventListener('wheel', (e) => {
        if (e.deltaY !== 0) {
            e.preventDefault();
            carousel.scrollLeft += e.deltaY;
        }
    }, { passive: false });

    let isDown = false;
    let startX;
    let scrollLeft;
    let hasDragged = false;

    carousel.addEventListener('mousedown', (e) => {
        isDown = true;
        hasDragged = false;
        carousel.classList.add('cursor-grabbing');
        startX = e.pageX - carousel.offsetLeft;
        scrollLeft = carousel.scrollLeft;
    });

    carousel.addEventListener('mouseleave', () => {
        isDown = false;
        carousel.classList.remove('cursor-grabbing');
    });

    carousel.addEventListener('mouseup', () => {
        isDown = false;
        carousel.classList.remove('cursor-grabbing');
    });

    carousel.addEventListener('mousemove', (e) => {
        if (!isDown) return;
        const x = e.pageX - carousel.offsetLeft;
        const walk = (x - startX) * 1.5;
        if (Math.abs(walk) > 4) {
            hasDragged = true;
        }
        e.preventDefault();
        carousel.scrollLeft = scrollLeft - walk;
    });

    carousel.addEventListener('click', (e) => {
        if (hasDragged) {
            e.stopImmediatePropagation();
            e.preventDefault();
            hasDragged = false;
        }
    }, true);
}

function saveCustomPresets() {
    localStorage.setItem('cs_custom_presets', JSON.stringify(customPresets));
    renderPresetCarousel();
    renderCustomPresetsList();
}

function movePresetUp(id) {
    const index = customPresets.findIndex(p => p.id === id);
    if (index > 0) {
        const temp = customPresets[ index ];
        customPresets[ index ] = customPresets[ index - 1 ];
        customPresets[ index - 1 ] = temp;
        saveCustomPresets();
    }
}

function movePresetDown(id) {
    const index = customPresets.findIndex(p => p.id === id);
    if (index !== -1 && index < customPresets.length - 1) {
        const temp = customPresets[ index ];
        customPresets[ index ] = customPresets[ index + 1 ];
        customPresets[ index + 1 ] = temp;
        saveCustomPresets();
    }
}

let editingPresetId = null;

function addOrUpdateCustomPreset() {
    const nameInput = document.getElementById('newPresetName');
    const promptInput = document.getElementById('newPresetPrompt');
    const name = nameInput ? nameInput.value.trim() : '';
    const prompt = promptInput ? promptInput.value.trim() : '';

    if (!name) {
        showToast("Please enter a menu item name.", true);
        if (nameInput) nameInput.focus();
        return;
    }
    if (!prompt) {
        showToast("Please enter system prompt instructions.", true);
        if (promptInput) promptInput.focus();
        return;
    }

    if (editingPresetId) {
        const preset = customPresets.find(p => p.id === editingPresetId);
        if (preset) {
            preset.name = name;
            preset.prompt = prompt;
            saveCustomPresets();
            showToast(`Updated custom preset "${name}"!`);
        }
        cancelEditCustomPreset();
    } else {
        const newPreset = {
            id: 'custom_' + Date.now(),
            name: name,
            prompt: prompt,
            isCustom: true
        };
        customPresets.push(newPreset);
        saveCustomPresets();
        if (nameInput) nameInput.value = '';
        if (promptInput) promptInput.value = '';
        showToast(`Added custom preset "${name}"!`);
    }
}

function editCustomPreset(id) {
    const preset = customPresets.find(p => p.id === id);
    if (!preset) return;
    editingPresetId = id;

    const nameInput = document.getElementById('newPresetName');
    const promptInput = document.getElementById('newPresetPrompt');
    const titleEl = document.getElementById('customPresetFormTitle');
    const btnText = document.getElementById('btnSavePresetText');
    const btnIcon = document.getElementById('btnSavePresetIcon');
    const cancelBtn = document.getElementById('btnCancelEditPreset');

    if (nameInput) nameInput.value = preset.name;
    if (promptInput) promptInput.value = preset.prompt;
    if (titleEl) {
        titleEl.innerHTML = `<i class="fa-solid fa-pen-to-square text-indigo-500"></i> Edit Preset: "${escapeHtml(preset.name)}"`;
    }
    if (btnText) btnText.textContent = "Save Changes";
    if (btnIcon) btnIcon.className = "fa-solid fa-check text-[10px]";
    if (cancelBtn) cancelBtn.classList.remove('hidden');

    if (nameInput) {
        nameInput.focus();
        nameInput.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}

function cancelEditCustomPreset() {
    editingPresetId = null;
    const nameInput = document.getElementById('newPresetName');
    const promptInput = document.getElementById('newPresetPrompt');
    const titleEl = document.getElementById('customPresetFormTitle');
    const btnText = document.getElementById('btnSavePresetText');
    const btnIcon = document.getElementById('btnSavePresetIcon');
    const cancelBtn = document.getElementById('btnCancelEditPreset');

    if (nameInput) nameInput.value = '';
    if (promptInput) promptInput.value = '';
    if (titleEl) {
        titleEl.innerHTML = `<i class="fa-solid fa-plus-circle text-indigo-500"></i> Create New Preset`;
    }
    if (btnText) btnText.textContent = "Add Preset";
    if (btnIcon) btnIcon.className = "fa-solid fa-plus text-[10px]";
    if (cancelBtn) cancelBtn.classList.add('hidden');
}

function deleteCustomPreset(id) {
    if (editingPresetId === id) {
        cancelEditCustomPreset();
    }
    const preset = customPresets.find(p => p.id === id);
    const presetName = preset ? preset.name : "Preset";
    customPresets = customPresets.filter(p => p.id !== id);
    saveCustomPresets();
    showToast(`Removed preset "${presetName}"`);
}

function renderPresetCarousel() {
    const container = document.getElementById('tonePresetsCarousel');
    if (!container) return;

    const builtInHtml = defaultPresets.map(preset => `
        <button onclick="triggerAI('${preset.id}')" title="${escapeHtml(preset.name)}: Built-in transformation"
            class="snap-start flex-shrink-0 flex items-center gap-2 px-4 py-2.5 ${preset.class} rounded-xl transition text-xs font-semibold shadow-sm active:scale-95 duration-75">
            <i class="fa-solid ${preset.icon}"></i> ${escapeHtml(preset.name)}
        </button>
    `).join('');

    const customHtml = customPresets.map(preset => `
        <button onclick="triggerAI('${preset.id}')" title="Custom Preset: ${escapeHtml(preset.prompt)}"
            class="snap-start flex-shrink-0 flex items-center gap-2 px-4 py-2.5 bg-fuchsia-50 dark:bg-fuchsia-500/10 border border-fuchsia-200 dark:border-fuchsia-500/30 text-fuchsia-600 dark:text-fuchsia-300 hover:bg-fuchsia-600 hover:text-white rounded-xl transition text-xs font-semibold shadow-sm active:scale-95 duration-75 group">
            <i class="fa-solid fa-sliders text-fuchsia-500 dark:text-fuchsia-400 group-hover:text-white"></i> ${escapeHtml(preset.name)}
            <span class="text-[9px] bg-fuchsia-200 dark:bg-fuchsia-900/60 text-fuchsia-800 dark:text-fuchsia-200 group-hover:bg-white/20 group-hover:text-white px-1.5 py-0.5 rounded font-mono uppercase tracking-wider">Custom</span>
        </button>
    `).join('');

    container.innerHTML = builtInHtml + customHtml;
}

function renderCustomPresetsList() {
    const container = document.getElementById('customPresetsContainer');
    if (!container) return;

    if (customPresets.length === 0) {
        container.innerHTML = `
            <div class="p-3 bg-slate-50/50 dark:bg-slate-900/30 border border-slate-200/50 dark:border-slate-800/40 rounded-xl text-center text-slate-400 text-xs italic">
                No custom presets created yet.
            </div>
        `;
        return;
    }

    container.innerHTML = customPresets.map((preset, index) => `
        <div class="p-3 bg-slate-50 dark:bg-slate-900/60 border border-slate-200 dark:border-slate-800 rounded-xl flex items-center justify-between gap-3">
            <div class="flex items-center gap-2 min-w-0">
                <div class="flex flex-col gap-0.5 flex-shrink-0">
                    <button onclick="movePresetUp('${preset.id}')" ${index === 0 ? 'disabled class="opacity-30 cursor-not-allowed"' : 'class="hover:text-indigo-500 transition"'} title="Move Up">
                        <i class="fa-solid fa-chevron-up text-[9px]"></i>
                    </button>
                    <button onclick="movePresetDown('${preset.id}')" ${index === customPresets.length - 1 ? 'disabled class="opacity-30 cursor-not-allowed"' : 'class="hover:text-indigo-500 transition"'} title="Move Down">
                        <i class="fa-solid fa-chevron-down text-[9px]"></i>
                    </button>
                </div>
                <div class="flex flex-col min-w-0">
                    <div class="flex items-center gap-2">
                        <span class="text-xs font-bold text-slate-800 dark:text-slate-100 truncate">${escapeHtml(preset.name)}</span>
                        <span class="text-[9px] bg-fuchsia-100 dark:bg-fuchsia-500/20 text-fuchsia-600 dark:text-fuchsia-400 px-1.5 py-0.5 rounded font-bold uppercase tracking-wider">Custom</span>
                    </div>
                    <p class="text-[11px] text-slate-500 dark:text-slate-400 line-clamp-1 italic mt-0.5">"${escapeHtml(preset.prompt)}"</p>
                </div>
            </div>
            <div class="flex items-center gap-1 flex-shrink-0">
                <button onclick="editCustomPreset('${preset.id}')"
                    class="text-indigo-600/80 hover:text-indigo-600 dark:text-indigo-400/80 dark:hover:text-indigo-300 p-1.5 hover:bg-indigo-500/10 rounded-lg transition"
                    title="Edit preset">
                    <i class="fa-solid fa-pen-to-square text-xs"></i>
                </button>
                <button onclick="deleteCustomPreset('${preset.id}')"
                    class="text-rose-500/70 hover:text-rose-600 dark:hover:text-rose-400 p-1.5 hover:bg-rose-500/10 rounded-lg transition"
                    title="Delete preset">
                    <i class="fa-solid fa-trash-can text-xs"></i>
                </button>
            </div>
        </div>
    `).join('');
}

function getPrompt(mode, text) {
    const customMatch = customPresets.find(p => p.id === mode);
    if (customMatch) {
        return `${customMatch.prompt}${STRICT_FORMATTING_SUFFIX}\n\nText: "${text}"`;
    }

    switch (mode) {
        case 'improve':
            return `Correct spelling, typos, and improve the general grammar of the following text.${STRICT_FORMATTING_SUFFIX}\n\nText: "${text}"`;
        case 'professional':
            return `Rewrite the following text to sound highly professional, formal, persuasive, and authoritative.${STRICT_FORMATTING_SUFFIX}\n\nText: "${text}"`;
        case 'casual':
            return `Rewrite the following text to sound friendly, relaxed, conversational, and natural. Keep it casual and engaging.${STRICT_FORMATTING_SUFFIX}\n\nText: "${text}"`;
        case 'summarize':
            return `Create a concise summary of the key message in the text. Return only 1 or 2 sentences of simple plain text.${STRICT_FORMATTING_SUFFIX}\n\nText: "${text}"`;
        case 'bullet':
            return `Extract the key bullet points from the text. Format them with simple dashes (-).${STRICT_FORMATTING_SUFFIX}\n\nText: "${text}"`;
        case 'expand':
            return `Elaborate on the following text by adding rich descriptive details, clarity, and depth while maintaining the exact core message.${STRICT_FORMATTING_SUFFIX}\n\nText: "${text}"`;
        case 'sarcastic':
            return `Rewrite the following text with sharp, clever sarcasm and dry humor while preserving the original meaning.${STRICT_FORMATTING_SUFFIX}\n\nText: "${text}"`;
        case 'prompt':
            return `Reconstruct the input text into a highly optimized, structured prompt tailored for advanced large language models (ChatGPT, Gemini).${STRICT_FORMATTING_SUFFIX}\n\nText: "${text}"`;
        default:
            return `Correct the spelling and improve readability.${STRICT_FORMATTING_SUFFIX}\n\nText: "${text}"`;
    }
}

// Text to Speech
function toggleSpeak() {
    const speakBtn = document.getElementById('speakBtn');
    const speakIcon = speakBtn ? speakBtn.querySelector('i') : null;

    if (isAndroid()) {
        if (window.AndroidBridge.isSpeaking()) {
            window.AndroidBridge.stopSpeaking();
            updateSpeechUI(false);
            showToast("Audio stopped");
        } else {
            const text = rawOutputText;
            if (!text || text === "Result will appear here...") {
                showToast("No text to play.", true);
                return;
            }
            window.AndroidBridge.speak(text);
            updateSpeechUI(true);
            showToast("Reading aloud...");
        }
        return;
    }

    // Web Speech API fallback
    if (!synth) {
        showToast("Speech audio not supported on this device.", true);
        return;
    }

    if (synth.speaking) {
        synth.cancel();
        updateSpeechUI(false);
        showToast("Audio stopped");
    } else {
        const text = rawOutputText;
        if (!text || text === "Result will appear here...") {
            showToast("No text to play.", true);
            return;
        }
        utterance = new SpeechSynthesisUtterance(text);
        utterance.onend = function () { updateSpeechUI(false); };
        utterance.onerror = function () { updateSpeechUI(false); };
        updateSpeechUI(true);
        synth.speak(utterance);
        showToast("Reading aloud...");
    }
}

function updateSpeechUI(speaking) {
    isSpeakingAudio = speaking;
    const speakBtn = document.getElementById('speakBtn');
    if (!speakBtn) return;
    const speakIcon = speakBtn.querySelector('i');
    if (speaking) {
        if (speakIcon) speakIcon.className = "fa-solid fa-circle-stop animate-pulse text-xs text-indigo-600 dark:text-indigo-400";
        speakBtn.classList.add('text-indigo-600', 'dark:text-indigo-400');
    } else {
        if (speakIcon) speakIcon.className = "fa-solid fa-volume-high text-xs";
        speakBtn.classList.remove('text-indigo-600', 'dark:text-indigo-400');
    }
}

// Callback from Kotlin MainActivity
window.onSpeechStateChanged = function(isSpeaking) {
    updateSpeechUI(isSpeaking);
};

// History Privacy & Retention
function getRetentionCutoff() {
    const retention = localStorage.getItem('cs_history_retention') || 'never';
    const customDays = parseFloat(localStorage.getItem('cs_custom_retention_days')) || 0;
    if (retention === 'never') return null;

    let days = 0;
    switch (retention) {
        case '1day': days = 1; break;
        case '3days': days = 3; break;
        case '1week': days = 7; break;
        case '1month': days = 30; break;
        case 'custom': days = customDays; break;
    }
    if (days <= 0) return null;
    return Date.now() - (days * 24 * 60 * 60 * 1000);
}

function pruneExpiredHistory(historyList) {
    const cutoff = getRetentionCutoff();
    if (!cutoff) return historyList;
    return historyList.filter(entry => entry.id >= cutoff);
}

function loadHistory() {
    const saved = localStorage.getItem('cs_history');
    if (saved) {
        try {
            history = JSON.parse(saved);
        } catch (e) {
            history = [];
        }
    } else {
        history = [];
    }

    const pruned = pruneExpiredHistory(history);
    if (pruned.length !== history.length) {
        history = pruned;
        localStorage.setItem('cs_history', JSON.stringify(history));
    }

    updateHistoryModeUI();
    renderHistory();
}

function escapeHtml(text) {
    if (!text) return "";
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function escapeJsString(str) {
    if (!str) return "";
    return str
        .replace(/\\/g, '\\\\')
        .replace(/'/g, "\\'")
        .replace(/"/g, '\\"')
        .replace(/\n/g, '\\n')
        .replace(/\r/g, '\\r');
}

function saveHistoryEntry(tone, inputVal, outputVal) {
    const historyMode = localStorage.getItem('cs_history_mode') || 'enabled';
    if (historyMode === 'incognito' || historyMode === 'disabled') {
        return;
    }

    const entry = {
        id: Date.now(),
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        date: new Date().toLocaleDateString([], { month: 'short', day: 'numeric' }),
        tone: tone,
        input: inputVal,
        output: outputVal
    };

    history.unshift(entry);
    if (history.length > 25) history.pop();
    history = pruneExpiredHistory(history);
    localStorage.setItem('cs_history', JSON.stringify(history));
    renderHistory();
}

function updateHistoryModeUI() {
    const mode = localStorage.getItem('cs_history_mode') || 'enabled';
    const isEnabled = mode === 'enabled';
    const isIncognito = mode === 'incognito';
    const isDisabled = mode === 'disabled';

    const incognitoBanner = document.getElementById('incognitoBannerDrawer');
    const disabledCard = document.getElementById('historyDisabledCard');
    const historyContainer = document.getElementById('historyContainer');
    const btnClearAll = document.getElementById('btnClearAllHistory');

    if (incognitoToggleBtn) {
        if (isDisabled) {
            incognitoToggleBtn.classList.add('hidden');
        } else {
            incognitoToggleBtn.classList.remove('hidden');
            if (isIncognito) {
                incognitoToggleBtn.classList.remove('text-slate-500', 'dark:text-slate-400', 'bg-slate-100', 'dark:bg-slate-800/40');
                incognitoToggleBtn.classList.add('bg-purple-500/20', 'text-purple-600', 'dark:text-purple-300', 'border-purple-500/40');
                incognitoToggleBtn.setAttribute('title', 'Incognito Mode: ON (Click to resume history logging)');
            } else {
                incognitoToggleBtn.classList.remove('bg-purple-500/20', 'text-purple-600', 'dark:text-purple-300', 'border-purple-500/40');
                incognitoToggleBtn.classList.add('text-slate-500', 'dark:text-slate-400', 'bg-slate-100', 'dark:bg-slate-800/40');
                incognitoToggleBtn.setAttribute('title', 'Incognito Mode: OFF (Click to pause history logging)');
            }
        }
    }

    if (incognitoBanner) incognitoBanner.classList.toggle('hidden', !isIncognito);
    if (disabledCard) {
        disabledCard.classList.toggle('hidden', !isDisabled);
        disabledCard.classList.toggle('flex', isDisabled);
    }
    if (historyContainer) historyContainer.classList.toggle('hidden', isDisabled);
    if (btnClearAll) btnClearAll.classList.toggle('hidden', isDisabled);
    if (historyModeSelect) historyModeSelect.value = mode;
}

function toggleIncognitoQuick() {
    const currentMode = localStorage.getItem('cs_history_mode') || 'enabled';
    const newMode = currentMode === 'incognito' ? 'enabled' : 'incognito';
    localStorage.setItem('cs_history_mode', newMode);
    if (isAndroid()) window.AndroidBridge.vibrateClick();
    updateHistoryModeUI();
    renderHistory();
    if (newMode === 'incognito') {
        showToast("Incognito Mode ON: History logging paused");
    } else {
        showToast("Incognito Mode OFF: History logging active");
    }
}

function getPresetDisplayName(toneOrMode) {
    if (!toneOrMode) return "PRESET";
    const custom = customPresets.find(p => p.id === toneOrMode || p.name === toneOrMode);
    if (custom) return custom.name;
    const builtIn = defaultPresets.find(p => p.id === toneOrMode || p.name === toneOrMode);
    if (builtIn) return builtIn.name;
    return toneOrMode;
}

function renderHistory() {
    const mode = localStorage.getItem('cs_history_mode') || 'enabled';
    if (mode === 'disabled') return;

    const container = document.getElementById('historyContainer');
    if (!container) return;

    history = pruneExpiredHistory(history);
    if (history.length === 0) {
        container.innerHTML = `
            <div class="flex flex-col items-center justify-center py-20 text-slate-400 dark:text-slate-600">
                <i class="fa-solid fa-clock-rotate-left text-4xl mb-3 opacity-30 animate-pulse"></i>
                <p class="text-xs">No previous logs saved yet</p>
            </div>
        `;
        return;
    }

    container.innerHTML = history.map(entry => `
        <div class="p-3 bg-slate-50 dark:bg-slate-900/60 border border-slate-200 dark:border-slate-800 rounded-xl hover:border-slate-300 dark:hover:border-slate-700/60 transition-all flex flex-col gap-1.5 relative group">
            <div class="flex justify-between items-center">
                <span class="text-[9px] font-bold uppercase tracking-wider text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-500/10 px-2 py-0.5 rounded border border-indigo-200 dark:border-indigo-500/10">${escapeHtml(getPresetDisplayName(entry.tone))}</span>
                <span class="text-[9px] text-slate-400 dark:text-slate-500">${entry.date} ${entry.timestamp}</span>
            </div>
            <p class="text-[11px] text-slate-600 dark:text-slate-400 line-clamp-2 italic">"${escapeHtml(entry.input)}"</p>
            <div class="flex justify-between items-center mt-2 pt-2 border-t border-slate-200 dark:border-slate-800/40">
                <button onclick="restoreHistory(${entry.id})" class="text-[10px] text-indigo-600 dark:text-indigo-400 hover:text-indigo-850 dark:hover:text-indigo-300 font-semibold flex items-center gap-1.5">
                    <i class="fa-solid fa-arrow-rotate-left"></i> Restore
                </button>
                <div class="flex gap-1.5 items-center">
                    <button onclick="copyText('${escapeJsString(entry.input)}', 'Original')" class="text-[10px] bg-slate-200/60 dark:bg-slate-800 text-slate-700 dark:text-slate-300 px-2 py-0.5 rounded font-semibold hover:bg-slate-300 dark:hover:bg-slate-700 transition">
                        <i class="fa-regular fa-copy"></i> Original
                    </button>
                    <button onclick="copyText('${escapeJsString(entry.output)}', 'Enhanced')" class="text-[10px] bg-indigo-600 text-white px-2 py-0.5 rounded font-semibold hover:bg-indigo-500 transition">
                        <i class="fa-regular fa-copy"></i> Enhanced
                    </button>
                    <button onclick="deleteHistory(${entry.id})" class="text-[10px] text-rose-600/50 dark:text-rose-500/55 hover:text-rose-400 transition ml-1" title="Delete log">
                        <i class="fa-solid fa-trash-can"></i>
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

function restoreHistory(id) {
    const entry = history.find(e => e.id === id);
    if (entry) {
        if (input) {
            input.value = entry.input;
            rawInputText = entry.input;
            input.dispatchEvent(new Event('input'));
        }
        rawOutputText = entry.output;
        if (window.innerWidth < 768) {
            switchTab('input');
        }
        renderOutput();
        toggleHistory();
        showToast("Restored from logs!");
    }
}

function deleteHistory(id) {
    history = history.filter(e => e.id !== id);
    localStorage.setItem('cs_history', JSON.stringify(history));
    if (isAndroid()) window.AndroidBridge.vibrateClick();
    renderHistory();
    showToast("Log removed");
}

function clearAllHistory() {
    if (confirm("Are you sure you want to clear your entire rewrite history?")) {
        history = [];
        localStorage.setItem('cs_history', JSON.stringify(history));
        if (isAndroid()) window.AndroidBridge.vibrateClick();
        renderHistory();
        showToast("All logs cleared.");
    }
}

function toggleHistory() {
    const drawer = document.getElementById('historyDrawer');
    const backdrop = document.getElementById('historyBackdrop');
    if (!drawer || !backdrop) return;
    drawer.classList.toggle('translate-x-full');
    backdrop.classList.toggle('opacity-0');
    backdrop.classList.toggle('pointer-events-none');
    if (isAndroid()) window.AndroidBridge.vibrateClick();
}

// Settings Dialog
function loadSettings() {
    const geminiKey = localStorage.getItem('cs_gemini_key') || '';
    const geminiModel = localStorage.getItem('cs_gemini_model') || 'gemini-3.5-flash-lite';
    const historyMode = localStorage.getItem('cs_history_mode') || 'enabled';
    const historyRetention = localStorage.getItem('cs_history_retention') || 'never';
    const customDays = localStorage.getItem('cs_custom_retention_days') || '';

    if (apiKeyInput) apiKeyInput.value = geminiKey;
    if (modelSelect) modelSelect.value = geminiModel;
    if (historyModeSelect) historyModeSelect.value = historyMode;
    if (historyRetentionSelect) historyRetentionSelect.value = historyRetention;
    if (customRetentionInput) customRetentionInput.value = customDays;

    toggleCustomRetention(historyRetention);
    updateProviderLabel();
    updateHistoryModeUI();
}

function saveSettings() {
    const btnApply = document.getElementById('btnApplySettings');
    const originalHtml = btnApply ? btnApply.innerHTML : '';
    const geminiKey = apiKeyInput ? apiKeyInput.value.trim() : '';
    const geminiModel = modelSelect ? modelSelect.value || 'gemini-3.5-flash-lite' : 'gemini-3.5-flash-lite';
    const historyMode = historyModeSelect ? historyModeSelect.value : 'enabled';
    const historyRetention = historyRetentionSelect ? historyRetentionSelect.value : 'never';
    const customDays = customRetentionInput ? customRetentionInput.value.trim() : '';

    if (historyRetention === 'custom') {
        const parsedDays = parseFloat(customDays);
        if (!customDays || isNaN(parsedDays) || parsedDays <= 0) {
            if (customRetentionInput) {
                customRetentionInput.classList.add('border-red-500', 'ring-1', 'ring-red-500');
                customRetentionInput.focus();
            }
            showToast("Custom retention duration is required (must be > 0 days)", true);
            return;
        }
    }

    if (customRetentionInput) {
        customRetentionInput.classList.remove('border-red-500', 'ring-1', 'ring-red-500');
    }

    localStorage.setItem('cs_gemini_key', geminiKey);
    localStorage.setItem('cs_gemini_model', geminiModel);
    localStorage.setItem('cs_history_mode', historyMode);
    localStorage.setItem('cs_history_retention', historyRetention);
    localStorage.setItem('cs_custom_retention_days', customDays);

    if (btnApply) {
        btnApply.disabled = true;
        btnApply.innerHTML = `<i class="fa-solid fa-check"></i> Settings Applied!`;
        btnApply.classList.replace('bg-indigo-600', 'bg-emerald-600');
    }

    updateProviderLabel();
    updateHistoryModeUI();
    renderHistory();
    if (isAndroid()) window.AndroidBridge.vibrateClick();
    showToast("Settings applied successfully!");

    setTimeout(() => {
        if (btnApply) {
            btnApply.disabled = false;
            btnApply.innerHTML = originalHtml;
            btnApply.classList.replace('bg-emerald-600', 'bg-indigo-600');
        }
        toggleModal();
    }, 600);
}

// Backup & Restore
function exportSettings() {
    const data = {
        exportDate: new Date().toISOString(),
        theme: localStorage.getItem('cs_theme') || 'dark',
        geminiModel: localStorage.getItem('cs_gemini_model') || 'gemini-3.5-flash-lite',
        historyMode: localStorage.getItem('cs_history_mode') || 'enabled',
        historyRetention: localStorage.getItem('cs_history_retention') || 'never',
        customRetentionDays: localStorage.getItem('cs_custom_retention_days') || '',
        customPresets: customPresets
    };

    const jsonStr = JSON.stringify(data, null, 2);

    if (isAndroid()) {
        window.AndroidBridge.exportSettings(jsonStr);
        return;
    }

    // Web fallback
    const blob = new Blob([ jsonStr ], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `verbatim-settings-backup-${new Date().toISOString().slice(0, 10)}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    showToast("Exported settings JSON (API key excluded)");
}

function triggerImportSettings() {
    if (isAndroid()) {
        window.AndroidBridge.triggerImportSettings();
        return;
    }
    const input = document.getElementById('importFileInput');
    if (input) input.click();
}

function importSettings(event) {
    const file = event.target.files[ 0 ];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = function (e) {
        processImportedJson(e.target.result);
        event.target.value = '';
    };
    reader.readAsText(file);
}

// Callback invoked by Android file reader or web file reader
window.handleImportedJson = function(jsonString) {
    processImportedJson(jsonString);
};

function processImportedJson(jsonString) {
    try {
        const settings = JSON.parse(jsonString);
        if (settings.theme) localStorage.setItem('cs_theme', settings.theme);
        if (settings.geminiModel) localStorage.setItem('cs_gemini_model', settings.geminiModel);
        if (settings.historyMode) localStorage.setItem('cs_history_mode', settings.historyMode);
        if (settings.historyRetention) localStorage.setItem('cs_history_retention', settings.historyRetention);
        if (settings.customRetentionDays !== undefined) localStorage.setItem('cs_custom_retention_days', settings.customRetentionDays);

        if (Array.isArray(settings.customPresets)) {
            customPresets = settings.customPresets.filter(p => p && typeof p.name === 'string' && typeof p.prompt === 'string').map(p => ({
                id: p.id || ('custom_' + Date.now() + Math.random().toString(36).substring(2, 5)),
                name: p.name.trim(),
                prompt: p.prompt.trim(),
                isCustom: true
            }));
            saveCustomPresets();
        }

        initTheme();
        loadSettings();
        showToast("Imported settings successfully!");
    } catch (err) {
        showToast(`Failed to import file: ${err.message}`, true);
    }
}

function toggleResetConfirm(show) {
    const box = document.getElementById('resetConfirmBox');
    if (box) {
        box.classList.toggle('hidden', !show);
    }
}

function executeResetToDefaults() {
    const clearLogs = document.getElementById('resetClearHistoryCheckbox')?.checked;
    localStorage.removeItem('cs_gemini_key');
    localStorage.setItem('cs_gemini_model', 'gemini-3.5-flash-lite');
    localStorage.setItem('cs_history_mode', 'enabled');
    localStorage.setItem('cs_history_retention', 'never');
    localStorage.setItem('cs_custom_retention_days', '');
    customPresets = [];
    saveCustomPresets();

    if (clearLogs) {
        history = [];
        localStorage.removeItem('cs_history');
    }

    toggleResetConfirm(false);
    loadSettings();
    renderHistory();
    if (isAndroid()) window.AndroidBridge.vibrateClick();
    showToast("Reset all settings to factory defaults!");
}

function toggleCustomRetention(retention) {
    if (!customRetentionSection || !customRetentionInput) return;
    if (retention === 'custom') {
        customRetentionSection.classList.remove('hidden');
        customRetentionInput.setAttribute('required', 'required');
    } else {
        customRetentionSection.classList.add('hidden');
        customRetentionInput.removeAttribute('required');
        customRetentionInput.classList.remove('border-red-500', 'ring-1', 'ring-red-500');
    }
}

function updateProviderLabel() {
    if (!currentProviderLabel) return;
    const hasKey = !!localStorage.getItem('cs_gemini_key');
    const dotColor = hasKey ? 'bg-green-500' : 'bg-red-500';
    currentProviderLabel.innerHTML = `
        <span class="w-1.5 h-1.5 ${dotColor} rounded-full"></span> Running: Google Gemini
    `;
}

function toggleModal() {
    const modal = document.getElementById('settingsModal');
    if (!modal) return;
    const active = !modal.classList.contains('opacity-0');
    if (active) {
        modal.classList.add('opacity-0', 'pointer-events-none');
    } else {
        modal.classList.remove('opacity-0', 'pointer-events-none');
    }
    if (isAndroid()) window.AndroidBridge.vibrateClick();
}

function setupVisibilityToggle(inputId, buttonId) {
    const field = document.getElementById(inputId);
    const button = document.getElementById(buttonId);
    if (!field || !button) return;
    const icon = button.querySelector('i');
    button.addEventListener('click', () => {
        const isPassword = field.type === 'password';
        field.type = isPassword ? 'text' : 'password';
        if (icon) icon.className = isPassword ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye';
    });
}

// Ping test connection
async function testConnection() {
    if (!testStatus) return;
    const key = (apiKeyInput ? apiKeyInput.value.trim() : '') || localStorage.getItem('cs_gemini_key');
    if (!key) {
        testStatus.style.display = 'inline-block';
        testStatus.className = 'text-rose-500 font-medium text-xs';
        testStatus.textContent = '❌ Missing API Key';
        return;
    }

    const selectedModel = (modelSelect ? modelSelect.value : '') || 'gemini-3.5-flash-lite';
    testStatus.style.display = 'inline-block';
    testStatus.className = 'text-slate-500 font-medium text-xs animate-pulse';
    testStatus.textContent = 'Testing connection...';

    try {
        const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${selectedModel}:generateContent`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'x-goog-api-key': key
            },
            body: JSON.stringify({
                contents: [ { parts: [ { text: "Ping test" } ] } ]
            })
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            const msg = errData.error?.message || `HTTP ${response.status}`;
            throw new Error(msg);
        }

        const data = await response.json();
        if (data?.candidates?.[ 0 ]?.content?.parts?.[ 0 ]?.text) {
            testStatus.className = 'text-emerald-500 font-semibold text-xs';
            testStatus.textContent = '✅ Connection successful!';
        } else {
            throw new Error("Invalid response");
        }
    } catch (err) {
        testStatus.className = 'text-rose-500 font-medium text-xs';
        testStatus.textContent = `❌ Test failed: ${err.message}`;
    }
}

// Text Editing Helpers
function clearText() {
    if (!input) return;
    input.value = '';
    rawInputText = '';
    rawOutputText = '';
    input.dispatchEvent(new Event('input'));
    input.focus();
    if (isAndroid()) window.AndroidBridge.vibrateClick();
}

async function pasteFromClipboard() {
    if (isAndroid()) {
        const text = window.AndroidBridge.pasteFromClipboard();
        if (text && input) {
            input.value = text;
            input.dispatchEvent(new Event('input'));
            input.focus();
            showToast("Pasted from clipboard!");
        } else {
            showToast("Clipboard is empty or inaccessible.", true);
        }
        return;
    }

    try {
        const text = await navigator.clipboard.readText();
        if (input) {
            input.value = text;
            input.dispatchEvent(new Event('input'));
            input.focus();
            showToast("Pasted from clipboard!");
        }
    } catch (err) {
        showToast("Failed to paste. Check permissions.", true);
    }
}

function copyToClipboard() {
    const textToCopy = output ? output.innerText : '';
    if (!textToCopy || textToCopy === "Result will appear here...") return;

    if (isAndroid()) {
        window.AndroidBridge.copyToClipboard(textToCopy, "Enhanced Output");
        showToast("Copied to clipboard!");
        return;
    }

    navigator.clipboard.writeText(textToCopy).then(() => {
        showToast("Copied to clipboard!");
    }).catch(() => {
        const textarea = document.createElement('textarea');
        textarea.value = textToCopy;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        showToast("Copied!");
    });
}

function copyText(str, label = "") {
    const msg = label ? `Copied ${label.toLowerCase()} text!` : "Copied to clipboard!";

    if (isAndroid()) {
        window.AndroidBridge.copyToClipboard(str, label);
        showToast(msg);
        return;
    }

    navigator.clipboard.writeText(str).then(() => {
        showToast(msg);
    }).catch(() => {
        const tempArea = document.createElement('textarea');
        tempArea.value = str;
        document.body.appendChild(tempArea);
        tempArea.select();
        document.execCommand('copy');
        document.body.removeChild(tempArea);
        showToast(msg);
    });
}

let toastTimer = null;
function showToast(msg, isError = false) {
    const toast = document.getElementById('toast');
    const toastMsg = document.getElementById('toastMsg');
    const toastIcon = document.getElementById('toastIcon');
    if (!toast || !toastMsg || !toastIcon) return;

    if (toastTimer) clearTimeout(toastTimer);
    toastMsg.textContent = msg;
    toastIcon.className = isError
        ? "fa-solid fa-triangle-exclamation text-rose-500 dark:text-rose-400 text-sm animate-bounce"
        : "fa-solid fa-circle-check text-green-600 dark:text-green-400 text-sm";

    toast.classList.remove('opacity-0', 'pointer-events-none');
    toastTimer = setTimeout(() => {
        toast.classList.add('opacity-0', 'pointer-events-none');
    }, 2500);
}

// Android Hardware Back Button Handler
window.handleAndroidBackPress = function() {
    // 1. Check if settings modal is open
    const modal = document.getElementById('settingsModal');
    if (modal && !modal.classList.contains('opacity-0')) {
        toggleModal();
        return true;
    }

    // 2. Check if history drawer is open
    const drawer = document.getElementById('historyDrawer');
    if (drawer && !drawer.classList.contains('translate-x-full')) {
        toggleHistory();
        return true;
    }

    // 3. If in Output tab on mobile, back to Input tab
    if (activeTab === 'output' && window.innerWidth < 768) {
        switchTab('input');
        return true;
    }

    return false;
};

// LCS Word-by-Word Diff
function diffWords(oldStr, newStr) {
    if (!oldStr) return escapeHtml(newStr);
    if (!newStr) return "";

    const oldWords = oldStr.split(/(\s+)/);
    const newWords = newStr.split(/(\s+)/);

    const dp = Array(oldWords.length + 1).fill(null).map(() => Array(newWords.length + 1).fill(0));

    for (let i = 1; i <= oldWords.length; i++) {
        for (let j = 1; j <= newWords.length; j++) {
            if (oldWords[ i - 1 ] === newWords[ j - 1 ]) {
                dp[ i ][ j ] = dp[ i - 1 ][ j - 1 ] + 1;
            } else {
                dp[ i ][ j ] = Math.max(dp[ i - 1 ][ j ], dp[ i ][ j - 1 ]);
            }
        }
    }

    let i = oldWords.length;
    let j = newWords.length;
    const diff = [];

    while (i > 0 || j > 0) {
        if (i > 0 && j > 0 && oldWords[ i - 1 ] === newWords[ j - 1 ]) {
            diff.unshift({ type: 'equal', value: oldWords[ i - 1 ] });
            i--;
            j--;
        } else if (j > 0 && (i === 0 || dp[ i ][ j - 1 ] >= dp[ i - 1 ][ j ])) {
            diff.unshift({ type: 'insert', value: newWords[ j - 1 ] });
            j--;
        } else {
            diff.unshift({ type: 'delete', value: oldWords[ i - 1 ] });
            i--;
        }
    }

    return diff.map(part => {
        if (part.type === 'insert') {
            return `<ins class="diff-added">${escapeHtml(part.value)}</ins>`;
        } else if (part.type === 'delete') {
            return `<del class="diff-removed">${escapeHtml(part.value)}</del>`;
        } else {
            return escapeHtml(part.value);
        }
    }).join('');
}
