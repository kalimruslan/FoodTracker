import { useState } from "react";

const COLORS = {
  primary: "#FF6B35",
  primaryLight: "#FF8F5E",
  primaryDark: "#E55A25",
  secondary: "#2EC4B6",
  secondaryLight: "#4DD4C8",
  accent: "#FFBF69",
  danger: "#E63946",
  success: "#2EC4B6",
  warning: "#FFBF69",
  protein: "#5B8DEF",
  carbs: "#FFBF69",
  fat: "#E63946",
  fiber: "#2EC4B6",
  bgLight: "#FAFAFA",
  bgCard: "#FFFFFF",
  bgDark: "#1A1A2E",
  bgDarkCard: "#252542",
  textPrimary: "#1A1A2E",
  textSecondary: "#6B7280",
  textLight: "#FFFFFF",
  border: "#E5E7EB",
};

const meals = [
  {
    id: 1,
    name: "Завтрак",
    icon: "🌅",
    time: "08:00",
    items: [
      { name: "Овсяная каша", cal: 150, weight: "200г" },
      { name: "Банан", cal: 89, weight: "1 шт" },
      { name: "Мёд", cal: 64, weight: "1 ст.л." },
    ],
  },
  {
    id: 2,
    name: "Обед",
    icon: "☀️",
    time: "13:00",
    items: [
      { name: "Куриная грудка", cal: 165, weight: "150г" },
      { name: "Рис бурый", cal: 216, weight: "180г" },
    ],
  },
  {
    id: 3,
    name: "Ужин",
    icon: "🌙",
    time: "19:00",
    items: [],
  },
  {
    id: 4,
    name: "Перекус",
    icon: "🍎",
    time: "",
    items: [],
  },
];

const searchResults = [
  { id: 1, name: "Куриная грудка", brand: "без бренда", cal: 110, per: "100г", protein: 23.1, carbs: 0, fat: 1.2, fiber: 0 },
  { id: 2, name: "Рис бурый варёный", brand: "без бренда", cal: 120, per: "100г", protein: 2.6, carbs: 24.7, fat: 0.8, fiber: 1.6 },
  { id: 3, name: "Гречневая каша", brand: "без бренда", cal: 132, per: "100г", protein: 4.5, carbs: 25.0, fat: 2.3, fiber: 3.7 },
  { id: 4, name: "Творог 5%", brand: "Домик в деревне", cal: 121, per: "100г", protein: 17.2, carbs: 1.8, fat: 5.0, fiber: 0 },
  { id: 5, name: "Яйцо куриное", brand: "без бренда", cal: 155, per: "100г", protein: 12.6, carbs: 0.7, fat: 10.6, fiber: 0 },
  { id: 6, name: "Овсянка на воде", brand: "без бренда", cal: 88, per: "100г", protein: 3.0, carbs: 15.0, fat: 1.7, fiber: 2.4 },
];

const weekDays = ["Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"];
const calHistory = [1850, 2100, 1950, 2250, 1780, 2050, 1680];

const CircularProgress = ({ value, max, size = 140, strokeWidth = 12, color = COLORS.primary, children }) => {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const progress = Math.min(value / max, 1);
  const offset = circumference * (1 - progress);

  return (
    <div style={{ position: "relative", width: size, height: size }}>
      <svg width={size} height={size} style={{ transform: "rotate(-90deg)" }}>
        <circle cx={size / 2} cy={size / 2} r={radius} fill="none" stroke={`${color}20`} strokeWidth={strokeWidth} />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          strokeLinecap="round"
          style={{ transition: "stroke-dashoffset 1s ease" }}
        />
      </svg>
      <div style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
        {children}
      </div>
    </div>
  );
};

const ProgressBar = ({ value, max, color, label, unit }) => {
  const pct = Math.min((value / max) * 100, 100);
  return (
    <div style={{ flex: 1 }}>
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 4 }}>
        <span style={{ fontSize: 11, fontWeight: 600, color: COLORS.textSecondary, letterSpacing: 0.5 }}>{label}</span>
        <span style={{ fontSize: 11, color: COLORS.textSecondary }}>{value}/{max}{unit}</span>
      </div>
      <div style={{ height: 8, borderRadius: 4, background: `${color}20`, overflow: "hidden" }}>
        <div style={{ height: "100%", width: `${pct}%`, borderRadius: 4, background: `linear-gradient(90deg, ${color}, ${color}CC)`, transition: "width 1s ease" }} />
      </div>
    </div>
  );
};

const MealCard = ({ meal, onAddClick }) => {
  const totalCal = meal.items.reduce((s, i) => s + i.cal, 0);
  return (
    <div style={{
      background: COLORS.bgCard,
      borderRadius: 16,
      padding: "14px 16px",
      marginBottom: 10,
      boxShadow: "0 2px 8px rgba(0,0,0,0.04)",
      border: `1px solid ${COLORS.border}`,
    }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: meal.items.length ? 10 : 0 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <span style={{ fontSize: 24 }}>{meal.icon}</span>
          <div>
            <div style={{ fontWeight: 700, fontSize: 15, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif" }}>{meal.name}</div>
            {meal.time && <div style={{ fontSize: 12, color: COLORS.textSecondary }}>{meal.time}</div>}
          </div>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          {totalCal > 0 && <span style={{ fontSize: 14, fontWeight: 700, color: COLORS.primary }}>{totalCal} ккал</span>}
          <button onClick={onAddClick} style={{
            width: 32, height: 32, borderRadius: 10, border: "none",
            background: `linear-gradient(135deg, ${COLORS.primary}, ${COLORS.primaryLight})`,
            color: "#fff", fontSize: 20, cursor: "pointer", display: "flex", alignItems: "center", justifyContent: "center",
            boxShadow: `0 2px 8px ${COLORS.primary}40`,
          }}>+</button>
        </div>
      </div>
      {meal.items.length > 0 && (
        <div style={{ borderTop: `1px solid ${COLORS.border}`, paddingTop: 8 }}>
          {meal.items.map((item, i) => (
            <div key={i} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "6px 0" }}>
              <div>
                <span style={{ fontSize: 13, color: COLORS.textPrimary }}>{item.name}</span>
                <span style={{ fontSize: 11, color: COLORS.textSecondary, marginLeft: 6 }}>{item.weight}</span>
              </div>
              <span style={{ fontSize: 13, color: COLORS.textSecondary, fontWeight: 600 }}>{item.cal}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

const HomeScreen = ({ onNavigate }) => {
  const [selectedDay, setSelectedDay] = useState(3);
  const consumed = 684;
  const target = 2200;
  const remaining = target - consumed;

  return (
    <div style={{ paddingBottom: 20 }}>
      {/* Header */}
      <div style={{
        background: `linear-gradient(135deg, ${COLORS.primary}, ${COLORS.primaryDark})`,
        borderRadius: "0 0 28px 28px",
        padding: "16px 20px 24px",
        color: "#fff",
      }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
          <div>
            <div style={{ fontSize: 13, opacity: 0.85, fontFamily: "'Nunito', sans-serif" }}>Сегодня</div>
            <div style={{ fontSize: 20, fontWeight: 800, fontFamily: "'Nunito', sans-serif" }}>12 февраля</div>
          </div>
          <div style={{ display: "flex", gap: 8 }}>
            <div style={{ width: 36, height: 36, borderRadius: 12, background: "rgba(255,255,255,0.2)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 16, cursor: "pointer" }}>📅</div>
            <div style={{ width: 36, height: 36, borderRadius: 12, background: "rgba(255,255,255,0.2)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 16, cursor: "pointer" }}>🔔</div>
          </div>
        </div>

        {/* Week days */}
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 20 }}>
          {weekDays.map((d, i) => (
            <div key={i} onClick={() => setSelectedDay(i)} style={{
              textAlign: "center", cursor: "pointer", padding: "6px 0", width: 40,
              borderRadius: 12, background: selectedDay === i ? "rgba(255,255,255,0.25)" : "transparent",
              transition: "background 0.2s",
            }}>
              <div style={{ fontSize: 10, opacity: 0.7, marginBottom: 2 }}>{d}</div>
              <div style={{ fontSize: 15, fontWeight: selectedDay === i ? 800 : 500 }}>{9 + i}</div>
            </div>
          ))}
        </div>

        {/* Circular + macros */}
        <div style={{ display: "flex", alignItems: "center", gap: 24 }}>
          <CircularProgress value={consumed} max={target} size={130} strokeWidth={10} color="#fff">
            <div style={{ fontSize: 28, fontWeight: 800, fontFamily: "'Nunito', sans-serif" }}>{consumed}</div>
            <div style={{ fontSize: 10, opacity: 0.8 }}>из {target} ккал</div>
            <div style={{ fontSize: 11, opacity: 0.7, marginTop: 2 }}>Осталось {remaining}</div>
          </CircularProgress>
          <div style={{ flex: 1, display: "flex", flexDirection: "column", gap: 10 }}>
            <MacroBarWhite label="Белки" value={56} max={140} color={COLORS.protein} unit="г" />
            <MacroBarWhite label="Жиры" value={22} max={73} color={COLORS.fat} unit="г" />
            <MacroBarWhite label="Углеводы" value={88} max={275} color={COLORS.carbs} unit="г" />
            <MacroBarWhite label="Клетчатка" value={8} max={30} color={COLORS.fiber} unit="г" />
          </div>
        </div>
      </div>

      {/* Meals */}
      <div style={{ padding: "16px 16px 0" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
          <span style={{ fontSize: 17, fontWeight: 800, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif" }}>Приёмы пищи</span>
          <span style={{ fontSize: 12, color: COLORS.primary, fontWeight: 600, cursor: "pointer" }}>+ Добавить приём</span>
        </div>
        {meals.map((m) => (
          <MealCard key={m.id} meal={m} onAddClick={() => onNavigate("search")} />
        ))}
      </div>

      {/* Water tracker mini */}
      <div style={{ padding: "0 16px" }}>
        <div style={{
          background: `linear-gradient(135deg, ${COLORS.secondary}15, ${COLORS.secondary}08)`,
          borderRadius: 16, padding: "14px 16px",
          border: `1px solid ${COLORS.secondary}30`,
          display: "flex", justifyContent: "space-between", alignItems: "center",
        }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <span style={{ fontSize: 24 }}>💧</span>
            <div>
              <div style={{ fontWeight: 700, fontSize: 14, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif" }}>Вода</div>
              <div style={{ fontSize: 12, color: COLORS.textSecondary }}>4 из 8 стаканов</div>
            </div>
          </div>
          <button style={{
            padding: "6px 14px", borderRadius: 10, border: "none",
            background: COLORS.secondary, color: "#fff", fontSize: 13, fontWeight: 600, cursor: "pointer",
          }}>+ Стакан</button>
        </div>
      </div>
    </div>
  );
};

const MacroBarWhite = ({ label, value, max, color, unit }) => {
  const pct = Math.min((value / max) * 100, 100);
  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 3 }}>
        <span style={{ fontSize: 11, fontWeight: 600, color: "rgba(255,255,255,0.85)" }}>{label}</span>
        <span style={{ fontSize: 11, color: "rgba(255,255,255,0.7)" }}>{value}/{max}{unit}</span>
      </div>
      <div style={{ height: 6, borderRadius: 3, background: "rgba(255,255,255,0.2)", overflow: "hidden" }}>
        <div style={{ height: "100%", width: `${pct}%`, borderRadius: 3, background: color, transition: "width 1s ease" }} />
      </div>
    </div>
  );
};

const SearchScreen = ({ onNavigate, onSelectProduct }) => {
  const [query, setQuery] = useState("");
  const [activeTab, setActiveTab] = useState("search");
  const [favorites, setFavorites] = useState(new Set([1, 4]));

  const tabs = [
    { id: "search", label: "Поиск", icon: "🔍" },
    { id: "recent", label: "Недавние", icon: "🕐" },
    { id: "favorites", label: "Избранное", icon: "⭐" },
    { id: "recipes", label: "Рецепты", icon: "📋" },
  ];

  const toggleFav = (id, e) => {
    e.stopPropagation();
    setFavorites((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  const filtered = query.length > 0
    ? searchResults.filter((p) => p.name.toLowerCase().includes(query.toLowerCase()))
    : searchResults;

  const displayResults = activeTab === "favorites"
    ? searchResults.filter((p) => favorites.has(p.id))
    : filtered;

  return (
    <div style={{ paddingBottom: 20 }}>
      {/* Header */}
      <div style={{ padding: "16px 16px 0" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 16 }}>
          <button onClick={() => onNavigate("home")} style={{ background: "none", border: "none", fontSize: 22, cursor: "pointer", padding: 0 }}>←</button>
          <span style={{ fontSize: 18, fontWeight: 800, fontFamily: "'Nunito', sans-serif", color: COLORS.textPrimary }}>Добавить продукт</span>
        </div>

        {/* Search bar */}
        <div style={{
          display: "flex", alignItems: "center", gap: 10, background: "#F3F4F6",
          borderRadius: 14, padding: "10px 14px", marginBottom: 14,
        }}>
          <span style={{ fontSize: 16, opacity: 0.5 }}>🔍</span>
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Найти продукт..."
            style={{
              border: "none", background: "transparent", flex: 1, fontSize: 15,
              outline: "none", color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif",
            }}
          />
          <div style={{
            width: 36, height: 36, borderRadius: 10,
            background: `linear-gradient(135deg, ${COLORS.primary}, ${COLORS.primaryLight})`,
            display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer",
            boxShadow: `0 2px 8px ${COLORS.primary}40`,
          }}>
            <span style={{ fontSize: 18 }}>📷</span>
          </div>
        </div>

        {/* Tabs */}
        <div style={{ display: "flex", gap: 6, marginBottom: 16, overflowX: "auto" }}>
          {tabs.map((t) => (
            <button key={t.id} onClick={() => setActiveTab(t.id)} style={{
              padding: "8px 14px", borderRadius: 12, border: "none", fontSize: 13, fontWeight: 600,
              cursor: "pointer", whiteSpace: "nowrap", fontFamily: "'Nunito', sans-serif",
              background: activeTab === t.id ? COLORS.primary : "#F3F4F6",
              color: activeTab === t.id ? "#fff" : COLORS.textSecondary,
              transition: "all 0.2s",
            }}>
              {t.icon} {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* Results */}
      <div style={{ padding: "0 16px" }}>
        {displayResults.map((p) => (
          <div key={p.id} onClick={() => onSelectProduct(p)} style={{
            background: COLORS.bgCard, borderRadius: 14, padding: "12px 14px", marginBottom: 8,
            boxShadow: "0 1px 4px rgba(0,0,0,0.04)", border: `1px solid ${COLORS.border}`,
            cursor: "pointer", transition: "transform 0.15s",
          }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 700, fontSize: 14, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif" }}>{p.name}</div>
                <div style={{ fontSize: 11, color: COLORS.textSecondary, marginTop: 2 }}>{p.brand} · {p.per}</div>
                <div style={{ display: "flex", gap: 12, marginTop: 8 }}>
                  <span style={{ fontSize: 11, color: COLORS.protein, fontWeight: 600 }}>Б {p.protein}г</span>
                  <span style={{ fontSize: 11, color: COLORS.fat, fontWeight: 600 }}>Ж {p.fat}г</span>
                  <span style={{ fontSize: 11, color: COLORS.carbs, fontWeight: 600 }}>У {p.carbs}г</span>
                </div>
              </div>
              <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: 6 }}>
                <span style={{ fontSize: 16, fontWeight: 800, color: COLORS.primary, fontFamily: "'Nunito', sans-serif" }}>{p.cal}</span>
                <span style={{ fontSize: 10, color: COLORS.textSecondary }}>ккал</span>
                <button onClick={(e) => toggleFav(p.id, e)} style={{
                  background: "none", border: "none", fontSize: 18, cursor: "pointer", padding: 0,
                }}>
                  {favorites.has(p.id) ? "⭐" : "☆"}
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const ProductScreen = ({ product, onNavigate }) => {
  const [weight, setWeight] = useState("100");
  const [unit, setUnit] = useState("г");
  const multiplier = parseFloat(weight || 0) / 100;

  const nutrients = [
    { label: "Калории", value: (product.cal * multiplier).toFixed(0), unit: "ккал", color: COLORS.primary },
    { label: "Белки", value: (product.protein * multiplier).toFixed(1), unit: "г", color: COLORS.protein },
    { label: "Жиры", value: (product.fat * multiplier).toFixed(1), unit: "г", color: COLORS.fat },
    { label: "Углеводы", value: (product.carbs * multiplier).toFixed(1), unit: "г", color: COLORS.carbs },
    { label: "Клетчатка", value: (product.fiber * multiplier).toFixed(1), unit: "г", color: COLORS.fiber },
  ];

  const micronutrients = [
    { label: "Натрий", value: "75 мг" },
    { label: "Калий", value: "256 мг" },
    { label: "Кальций", value: "12 мг" },
    { label: "Железо", value: "0.7 мг" },
    { label: "Витамин A", value: "6 мкг" },
    { label: "Витамин C", value: "0 мг" },
  ];

  return (
    <div style={{ paddingBottom: 100 }}>
      {/* Header */}
      <div style={{
        background: `linear-gradient(135deg, ${COLORS.primary}, ${COLORS.primaryDark})`,
        borderRadius: "0 0 28px 28px",
        padding: "16px 20px 28px",
        color: "#fff",
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 20 }}>
          <button onClick={() => onNavigate("search")} style={{ background: "none", border: "none", fontSize: 22, cursor: "pointer", padding: 0, color: "#fff" }}>←</button>
          <span style={{ fontSize: 18, fontWeight: 800, fontFamily: "'Nunito', sans-serif" }}>Продукт</span>
        </div>

        <div style={{ fontSize: 22, fontWeight: 800, fontFamily: "'Nunito', sans-serif", marginBottom: 4 }}>{product.name}</div>
        <div style={{ fontSize: 13, opacity: 0.8, marginBottom: 20 }}>{product.brand}</div>

        {/* Big calorie circle */}
        <div style={{ display: "flex", justifyContent: "center", marginBottom: 8 }}>
          <CircularProgress value={product.cal * multiplier} max={500} size={150} strokeWidth={12} color="#fff">
            <div style={{ fontSize: 36, fontWeight: 800, fontFamily: "'Nunito', sans-serif" }}>{(product.cal * multiplier).toFixed(0)}</div>
            <div style={{ fontSize: 12, opacity: 0.8 }}>ккал</div>
          </CircularProgress>
        </div>
      </div>

      {/* Weight input */}
      <div style={{ padding: "16px 16px 0" }}>
        <div style={{
          background: COLORS.bgCard, borderRadius: 16, padding: "14px 16px",
          boxShadow: "0 2px 8px rgba(0,0,0,0.04)", border: `1px solid ${COLORS.border}`,
          display: "flex", alignItems: "center", gap: 10, marginBottom: 16,
        }}>
          <span style={{ fontSize: 14, fontWeight: 700, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif" }}>Порция:</span>
          <input
            value={weight}
            onChange={(e) => setWeight(e.target.value)}
            style={{
              width: 60, border: `2px solid ${COLORS.primary}30`, borderRadius: 10, padding: "8px 10px",
              fontSize: 16, fontWeight: 700, textAlign: "center", outline: "none", color: COLORS.textPrimary,
              fontFamily: "'Nunito', sans-serif",
            }}
          />
          <div style={{ display: "flex", gap: 4 }}>
            {["г", "шт", "порц"].map((u) => (
              <button key={u} onClick={() => setUnit(u)} style={{
                padding: "8px 12px", borderRadius: 10, border: "none", fontSize: 12, fontWeight: 600,
                cursor: "pointer", fontFamily: "'Nunito', sans-serif",
                background: unit === u ? COLORS.primary : "#F3F4F6",
                color: unit === u ? "#fff" : COLORS.textSecondary,
              }}>{u}</button>
            ))}
          </div>
        </div>

        {/* Macros grid */}
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8, marginBottom: 16 }}>
          {nutrients.map((n, i) => (
            <div key={i} style={{
              background: COLORS.bgCard, borderRadius: 14, padding: "14px",
              boxShadow: "0 1px 4px rgba(0,0,0,0.04)", border: `1px solid ${COLORS.border}`,
              gridColumn: i === 0 ? "1 / -1" : "auto",
            }}>
              <div style={{ fontSize: 11, color: COLORS.textSecondary, fontWeight: 600, marginBottom: 4 }}>{n.label}</div>
              <div style={{ display: "flex", alignItems: "baseline", gap: 4 }}>
                <span style={{ fontSize: i === 0 ? 28 : 22, fontWeight: 800, color: n.color, fontFamily: "'Nunito', sans-serif" }}>{n.value}</span>
                <span style={{ fontSize: 12, color: COLORS.textSecondary }}>{n.unit}</span>
              </div>
            </div>
          ))}
        </div>

        {/* Micronutrients */}
        <div style={{ fontSize: 16, fontWeight: 800, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif", marginBottom: 10 }}>Микронутриенты</div>
        <div style={{
          background: COLORS.bgCard, borderRadius: 16, overflow: "hidden",
          boxShadow: "0 1px 4px rgba(0,0,0,0.04)", border: `1px solid ${COLORS.border}`,
        }}>
          {micronutrients.map((m, i) => (
            <div key={i} style={{
              display: "flex", justifyContent: "space-between", padding: "12px 16px",
              borderBottom: i < micronutrients.length - 1 ? `1px solid ${COLORS.border}` : "none",
            }}>
              <span style={{ fontSize: 13, color: COLORS.textPrimary }}>{m.label}</span>
              <span style={{ fontSize: 13, fontWeight: 600, color: COLORS.textSecondary }}>{m.value}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Add button */}
      <div style={{
        position: "sticky", bottom: 0, padding: "12px 16px 16px",
        background: "linear-gradient(transparent, #FAFAFA 30%)",
      }}>
        <button onClick={() => onNavigate("home")} style={{
          width: "100%", padding: "16px", borderRadius: 16, border: "none",
          background: `linear-gradient(135deg, ${COLORS.primary}, ${COLORS.primaryLight})`,
          color: "#fff", fontSize: 16, fontWeight: 700, cursor: "pointer",
          fontFamily: "'Nunito', sans-serif",
          boxShadow: `0 4px 16px ${COLORS.primary}50`,
        }}>
          Добавить в Завтрак
        </button>
      </div>
    </div>
  );
};

const ProfileScreen = ({ onNavigate }) => {
  const [activeGoal, setActiveGoal] = useState("lose");
  const goals = [
    { id: "lose", label: "Похудеть", icon: "📉" },
    { id: "maintain", label: "Поддержать", icon: "⚖️" },
    { id: "gain", label: "Набрать", icon: "📈" },
  ];

  const weightData = [135, 133.5, 132, 131.5, 130.8, 130.2, 130];
  const weightLabels = ["Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл"];
  const maxW = Math.max(...weightData);
  const minW = Math.min(...weightData);

  return (
    <div style={{ paddingBottom: 20 }}>
      {/* Profile header */}
      <div style={{
        background: `linear-gradient(135deg, ${COLORS.primary}, ${COLORS.primaryDark})`,
        borderRadius: "0 0 28px 28px",
        padding: "16px 20px 28px",
        color: "#fff",
        textAlign: "center",
      }}>
        <div style={{ fontSize: 18, fontWeight: 800, fontFamily: "'Nunito', sans-serif", marginBottom: 20 }}>Профиль</div>
        <div style={{
          width: 80, height: 80, borderRadius: "50%", background: "rgba(255,255,255,0.2)",
          margin: "0 auto 12px", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 36,
        }}>👤</div>
        <div style={{ fontSize: 20, fontWeight: 800, fontFamily: "'Nunito', sans-serif" }}>Пользователь</div>
        <div style={{ fontSize: 13, opacity: 0.8, marginTop: 4 }}>Цель: 2 200 ккал/день</div>
      </div>

      <div style={{ padding: "16px" }}>
        {/* Goal selection */}
        <div style={{ fontSize: 16, fontWeight: 800, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif", marginBottom: 10 }}>Моя цель</div>
        <div style={{ display: "flex", gap: 8, marginBottom: 20 }}>
          {goals.map((g) => (
            <button key={g.id} onClick={() => setActiveGoal(g.id)} style={{
              flex: 1, padding: "14px 8px", borderRadius: 14, border: activeGoal === g.id ? `2px solid ${COLORS.primary}` : `1px solid ${COLORS.border}`,
              background: activeGoal === g.id ? `${COLORS.primary}10` : COLORS.bgCard,
              cursor: "pointer", textAlign: "center",
              boxShadow: activeGoal === g.id ? `0 2px 12px ${COLORS.primary}20` : "none",
            }}>
              <div style={{ fontSize: 24, marginBottom: 4 }}>{g.icon}</div>
              <div style={{ fontSize: 12, fontWeight: 700, color: activeGoal === g.id ? COLORS.primary : COLORS.textSecondary, fontFamily: "'Nunito', sans-serif" }}>{g.label}</div>
            </button>
          ))}
        </div>

        {/* Body params */}
        <div style={{ fontSize: 16, fontWeight: 800, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif", marginBottom: 10 }}>Параметры тела</div>
        <div style={{
          background: COLORS.bgCard, borderRadius: 16, padding: "4px 0", marginBottom: 20,
          boxShadow: "0 2px 8px rgba(0,0,0,0.04)", border: `1px solid ${COLORS.border}`,
        }}>
          {[
            { label: "Вес", value: "130 кг", icon: "⚖️" },
            { label: "Рост", value: "185 см", icon: "📏" },
            { label: "Возраст", value: "30 лет", icon: "🎂" },
            { label: "Пол", value: "Мужской", icon: "👨" },
            { label: "Активность", value: "Умеренная", icon: "🏃" },
            { label: "Темп", value: "0.5 кг/нед", icon: "⏱️" },
          ].map((p, i) => (
            <div key={i} style={{
              display: "flex", alignItems: "center", padding: "14px 16px",
              borderBottom: i < 5 ? `1px solid ${COLORS.border}` : "none",
              cursor: "pointer",
            }}>
              <span style={{ fontSize: 20, marginRight: 12 }}>{p.icon}</span>
              <span style={{ flex: 1, fontSize: 14, color: COLORS.textPrimary, fontWeight: 500 }}>{p.label}</span>
              <span style={{ fontSize: 14, fontWeight: 700, color: COLORS.primary, fontFamily: "'Nunito', sans-serif" }}>{p.value}</span>
              <span style={{ marginLeft: 8, color: COLORS.textSecondary, fontSize: 14 }}>›</span>
            </div>
          ))}
        </div>

        {/* Calorie calculation */}
        <div style={{
          background: `linear-gradient(135deg, ${COLORS.secondary}15, ${COLORS.secondary}05)`,
          borderRadius: 16, padding: 16, marginBottom: 20,
          border: `1px solid ${COLORS.secondary}30`,
        }}>
          <div style={{ fontSize: 14, fontWeight: 800, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif", marginBottom: 10 }}>
            Расчёт по Миффлина-Сан Жеора
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
            <span style={{ fontSize: 13, color: COLORS.textSecondary }}>Базовый метаболизм (BMR)</span>
            <span style={{ fontSize: 13, fontWeight: 700, color: COLORS.textPrimary }}>2 050 ккал</span>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
            <span style={{ fontSize: 13, color: COLORS.textSecondary }}>С учётом активности (TDEE)</span>
            <span style={{ fontSize: 13, fontWeight: 700, color: COLORS.textPrimary }}>2 700 ккал</span>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", padding: "8px 0 0", borderTop: `1px solid ${COLORS.secondary}30` }}>
            <span style={{ fontSize: 14, fontWeight: 700, color: COLORS.secondary }}>Цель (дефицит -500)</span>
            <span style={{ fontSize: 16, fontWeight: 800, color: COLORS.secondary, fontFamily: "'Nunito', sans-serif" }}>2 200 ккал</span>
          </div>
        </div>

        {/* Weight chart */}
        <div style={{ fontSize: 16, fontWeight: 800, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif", marginBottom: 10 }}>
          Динамика веса
        </div>
        <div style={{
          background: COLORS.bgCard, borderRadius: 16, padding: 16,
          boxShadow: "0 2px 8px rgba(0,0,0,0.04)", border: `1px solid ${COLORS.border}`,
          marginBottom: 16,
        }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", height: 120, marginBottom: 8, gap: 4 }}>
            {weightData.map((w, i) => {
              const h = ((maxW - w) / (maxW - minW + 2)) * 80 + 30;
              return (
                <div key={i} style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "flex-end", height: "100%" }}>
                  <span style={{ fontSize: 10, fontWeight: 700, color: COLORS.primary, marginBottom: 4 }}>{w}</span>
                  <div style={{
                    width: "70%", height: h, borderRadius: 8,
                    background: i === weightData.length - 1
                      ? `linear-gradient(180deg, ${COLORS.primary}, ${COLORS.primaryLight})`
                      : `linear-gradient(180deg, ${COLORS.primary}40, ${COLORS.primary}20)`,
                  }} />
                </div>
              );
            })}
          </div>
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            {weightLabels.map((l, i) => (
              <span key={i} style={{ flex: 1, textAlign: "center", fontSize: 10, color: COLORS.textSecondary }}>{l}</span>
            ))}
          </div>
          <div style={{ textAlign: "center", marginTop: 12 }}>
            <span style={{ fontSize: 12, color: COLORS.success, fontWeight: 700 }}>↓ -5 кг за 7 месяцев</span>
          </div>
        </div>

        {/* Photo progress */}
        <div style={{ fontSize: 16, fontWeight: 800, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif", marginBottom: 10 }}>Фото прогресса</div>
        <div style={{ display: "flex", gap: 8, marginBottom: 20 }}>
          {["Янв", "Апр", "Июл"].map((m, i) => (
            <div key={i} style={{
              flex: 1, height: 130, borderRadius: 14, background: "#E5E7EB",
              display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center",
              border: `1px solid ${COLORS.border}`,
            }}>
              <span style={{ fontSize: 28, marginBottom: 4 }}>📷</span>
              <span style={{ fontSize: 11, color: COLORS.textSecondary, fontWeight: 600 }}>{m}</span>
            </div>
          ))}
          <div style={{
            width: 50, height: 130, borderRadius: 14, background: `${COLORS.primary}10`,
            display: "flex", alignItems: "center", justifyContent: "center",
            border: `2px dashed ${COLORS.primary}40`, cursor: "pointer",
          }}>
            <span style={{ fontSize: 24, color: COLORS.primary }}>+</span>
          </div>
        </div>

        {/* Reminders */}
        <div style={{ fontSize: 16, fontWeight: 800, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif", marginBottom: 10 }}>Напоминания</div>
        <div style={{
          background: COLORS.bgCard, borderRadius: 16, padding: "4px 0",
          boxShadow: "0 2px 8px rgba(0,0,0,0.04)", border: `1px solid ${COLORS.border}`,
        }}>
          {[
            { label: "Завтрак", time: "08:00", enabled: true },
            { label: "Обед", time: "13:00", enabled: true },
            { label: "Ужин", time: "19:00", enabled: true },
            { label: "Умные напоминания", time: "", enabled: true },
          ].map((r, i) => (
            <div key={i} style={{
              display: "flex", alignItems: "center", padding: "14px 16px",
              borderBottom: i < 3 ? `1px solid ${COLORS.border}` : "none",
            }}>
              <span style={{ flex: 1, fontSize: 14, color: COLORS.textPrimary }}>{r.label}</span>
              {r.time && <span style={{ fontSize: 13, color: COLORS.textSecondary, marginRight: 12 }}>{r.time}</span>}
              <div style={{
                width: 44, height: 26, borderRadius: 13, padding: 2, cursor: "pointer",
                background: r.enabled ? COLORS.primary : "#D1D5DB",
                transition: "background 0.2s",
              }}>
                <div style={{
                  width: 22, height: 22, borderRadius: "50%", background: "#fff",
                  transform: r.enabled ? "translateX(18px)" : "translateX(0)",
                  transition: "transform 0.2s",
                  boxShadow: "0 1px 3px rgba(0,0,0,0.15)",
                }} />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

const StatsScreen = () => {
  const maxCal = Math.max(...calHistory);
  return (
    <div style={{ paddingBottom: 20 }}>
      <div style={{
        background: `linear-gradient(135deg, ${COLORS.primary}, ${COLORS.primaryDark})`,
        borderRadius: "0 0 28px 28px",
        padding: "16px 20px 28px",
        color: "#fff",
      }}>
        <div style={{ fontSize: 18, fontWeight: 800, fontFamily: "'Nunito', sans-serif", marginBottom: 16, textAlign: "center" }}>Статистика</div>

        <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
          {["Неделя", "Месяц", "3 мес"].map((p, i) => (
            <button key={i} style={{
              flex: 1, padding: "8px", borderRadius: 10, border: "none", fontSize: 13, fontWeight: 600,
              cursor: "pointer", fontFamily: "'Nunito', sans-serif",
              background: i === 0 ? "rgba(255,255,255,0.25)" : "rgba(255,255,255,0.1)",
              color: "#fff",
            }}>{p}</button>
          ))}
        </div>

        {/* Weekly bar chart */}
        <div style={{ display: "flex", alignItems: "flex-end", justifyContent: "space-between", height: 130, gap: 6 }}>
          {calHistory.map((c, i) => {
            const h = (c / maxCal) * 100;
            const isOver = c > 2200;
            return (
              <div key={i} style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "flex-end", height: "100%" }}>
                <span style={{ fontSize: 9, marginBottom: 4, opacity: 0.8 }}>{c}</span>
                <div style={{
                  width: "70%", height: `${h}%`, borderRadius: 8,
                  background: isOver
                    ? `linear-gradient(180deg, ${COLORS.danger}, ${COLORS.danger}AA)`
                    : "linear-gradient(180deg, rgba(255,255,255,0.9), rgba(255,255,255,0.5))",
                }} />
              </div>
            );
          })}
        </div>
        <div style={{ display: "flex", justifyContent: "space-between", marginTop: 6 }}>
          {weekDays.map((d, i) => (
            <span key={i} style={{ flex: 1, textAlign: "center", fontSize: 10, opacity: 0.7 }}>{d}</span>
          ))}
        </div>
        <div style={{ display: "flex", justifyContent: "center", gap: 16, marginTop: 10 }}>
          <span style={{ fontSize: 10, opacity: 0.7 }}>⬜ в норме</span>
          <span style={{ fontSize: 10, opacity: 0.7 }}>🟥 превышение</span>
        </div>
      </div>

      <div style={{ padding: "16px" }}>
        {/* Average stats */}
        <div style={{ fontSize: 16, fontWeight: 800, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif", marginBottom: 10 }}>Средние показатели</div>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8, marginBottom: 20 }}>
          {[
            { label: "Калории", value: "1 951", unit: "ккал/день", color: COLORS.primary, icon: "🔥" },
            { label: "Белки", value: "98", unit: "г/день", color: COLORS.protein, icon: "🥩" },
            { label: "Жиры", value: "62", unit: "г/день", color: COLORS.fat, icon: "🥑" },
            { label: "Углеводы", value: "215", unit: "г/день", color: COLORS.carbs, icon: "🍞" },
          ].map((s, i) => (
            <div key={i} style={{
              background: COLORS.bgCard, borderRadius: 14, padding: 14,
              boxShadow: "0 1px 4px rgba(0,0,0,0.04)", border: `1px solid ${COLORS.border}`,
            }}>
              <div style={{ fontSize: 20, marginBottom: 6 }}>{s.icon}</div>
              <div style={{ fontSize: 11, color: COLORS.textSecondary, fontWeight: 600 }}>{s.label}</div>
              <div style={{ fontSize: 22, fontWeight: 800, color: s.color, fontFamily: "'Nunito', sans-serif" }}>{s.value}</div>
              <div style={{ fontSize: 10, color: COLORS.textSecondary }}>{s.unit}</div>
            </div>
          ))}
        </div>

        {/* Daily macro breakdown */}
        <div style={{ fontSize: 16, fontWeight: 800, color: COLORS.textPrimary, fontFamily: "'Nunito', sans-serif", marginBottom: 10 }}>Распределение БЖУ</div>
        <div style={{
          background: COLORS.bgCard, borderRadius: 16, padding: 16,
          boxShadow: "0 2px 8px rgba(0,0,0,0.04)", border: `1px solid ${COLORS.border}`,
        }}>
          <div style={{ display: "flex", height: 20, borderRadius: 10, overflow: "hidden", marginBottom: 12 }}>
            <div style={{ width: "26%", background: COLORS.protein }} />
            <div style={{ width: "33%", background: COLORS.fat }} />
            <div style={{ width: "41%", background: COLORS.carbs }} />
          </div>
          <div style={{ display: "flex", justifyContent: "space-around" }}>
            {[
              { label: "Белки", pct: "26%", color: COLORS.protein },
              { label: "Жиры", pct: "33%", color: COLORS.fat },
              { label: "Углеводы", pct: "41%", color: COLORS.carbs },
            ].map((m, i) => (
              <div key={i} style={{ textAlign: "center" }}>
                <div style={{ width: 10, height: 10, borderRadius: "50%", background: m.color, margin: "0 auto 4px" }} />
                <div style={{ fontSize: 13, fontWeight: 700, color: COLORS.textPrimary }}>{m.pct}</div>
                <div style={{ fontSize: 10, color: COLORS.textSecondary }}>{m.label}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default function FoodTracker() {
  const [screen, setScreen] = useState("home");
  const [selectedProduct, setSelectedProduct] = useState(null);

  const handleSelectProduct = (product) => {
    setSelectedProduct(product);
    setScreen("product");
  };

  const navItems = [
    { id: "home", label: "Главная", icon: "🏠" },
    { id: "search", label: "Поиск", icon: "🔍" },
    { id: "stats", label: "Статистика", icon: "📊" },
    { id: "profile", label: "Профиль", icon: "👤" },
  ];

  return (
    <div style={{
      maxWidth: 390,
      margin: "0 auto",
      background: COLORS.bgLight,
      minHeight: "100vh",
      fontFamily: "'Nunito', 'Segoe UI', sans-serif",
      position: "relative",
      paddingBottom: 70,
      boxShadow: "0 0 60px rgba(0,0,0,0.1)",
    }}>
      <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;500;600;700;800;900&display=swap" rel="stylesheet" />

      {screen === "home" && <HomeScreen onNavigate={setScreen} />}
      {screen === "search" && <SearchScreen onNavigate={setScreen} onSelectProduct={handleSelectProduct} />}
      {screen === "product" && selectedProduct && <ProductScreen product={selectedProduct} onNavigate={setScreen} />}
      {screen === "profile" && <ProfileScreen onNavigate={setScreen} />}
      {screen === "stats" && <StatsScreen />}

      {/* Bottom Navigation */}
      <div style={{
        position: "fixed",
        bottom: 0,
        left: "50%",
        transform: "translateX(-50%)",
        width: 390,
        background: "#fff",
        borderTop: `1px solid ${COLORS.border}`,
        display: "flex",
        justifyContent: "space-around",
        padding: "8px 0 12px",
        zIndex: 100,
      }}>
        {navItems.map((item) => (
          <button
            key={item.id}
            onClick={() => setScreen(item.id)}
            style={{
              background: "none",
              border: "none",
              cursor: "pointer",
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              gap: 2,
              padding: "4px 12px",
              borderRadius: 12,
              transition: "all 0.2s",
            }}
          >
            <span style={{
              fontSize: 22,
              filter: screen === item.id ? "none" : "grayscale(0.8)",
              opacity: screen === item.id ? 1 : 0.5,
            }}>{item.icon}</span>
            <span style={{
              fontSize: 10,
              fontWeight: screen === item.id ? 700 : 500,
              color: screen === item.id ? COLORS.primary : COLORS.textSecondary,
              fontFamily: "'Nunito', sans-serif",
            }}>{item.label}</span>
            {screen === item.id && (
              <div style={{
                width: 4, height: 4, borderRadius: "50%",
                background: COLORS.primary,
                marginTop: 1,
              }} />
            )}
          </button>
        ))}
      </div>
    </div>
  );
}
