import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import "../styles/people.css";
import { ReactComponent as SearchIcon } from "../icons/search.svg";
import { ReactComponent as ChevronUpIcon } from "../icons/chevron-up.svg";
import { ReactComponent as ChevronDownIcon } from "../icons/chevron-down.svg";
import { ReactComponent as UserAvatarIcon } from "../icons/user-avatar.svg";
import PersonProfileView from "../components/PersonProfileView";
import type { Department, TitleEntity, Person } from "../types/models";



type SortState = { sortBy: keyof Person | "id" | "person" | "department.name" | "title.name"; direction: "asc" | "desc" };

type Filters = {
  q: string;
  departmentId: string;
  titleId: string;
  gender: string;
};

const defaultFilters: Filters = {
  q: "",
  departmentId: "",
  titleId: "",
  gender: "",
};

export default function People() {
  const [rows, setRows] = useState<Person[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [sort, setSort] = useState<SortState>({ sortBy: "id", direction: "asc" });
  const [filters, setFilters] = useState<Filters>(defaultFilters);
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [departments, setDepartments] = useState<Department[]>([]);
  const [titles, setTitles] = useState<TitleEntity[]>([]);

  const [selectedPerson, setSelectedPerson] = useState<Person | null>(null);

  const columns: Array<{ key: keyof Person | "id" | "person" | "actions"; label: string }> = useMemo(
    () => [
      { key: "person", label: "Person" },
      { key: "email", label: "Email" },
      { key: "phoneNumber", label: "Phone" },
      { key: "department", label: "Department" },
      { key: "title", label: "Title" },
      { key: "gender", label: "Gender" },
      { key: "address", label: "Address" },
      { key: "actions", label: "" },
    ],
    []
  );

  useEffect(() => {
    const loadMeta = async () => {
      try {
        const [depRes, titleRes] = await Promise.all([
          axios.get<Department[]>("http://localhost:8081/api/departments"),
          axios.get<TitleEntity[]>("http://localhost:8081/api/titles"),
        ]);
        setDepartments(depRes.data);
        setTitles(titleRes.data);
      } catch {
      }
    };
    loadMeta();
  }, []);

  useEffect(() => {
    if (selectedPerson) return;
    const fetchData = async () => {
      setLoading(true);
      setError(null);
      try {
        const params: Record<string, string | number> = {
          page,
          size,
          sortBy: sort.sortBy,
          direction: sort.direction,
        };
        Object.entries(filters).forEach(([k, v]) => {
          if (v) params[k] = v;
        });
        const res = await axios.get(
          "http://localhost:8081/api/people",
          { params }
        );
        const data: any = res.data;
        const content: Person[] = Array.isArray(data) ? data : (data?.content ?? []);
        const totalElements: number = Array.isArray(data) ? data.length : (data?.totalElements ?? content.length);
        setRows(content);
        setTotal(totalElements);
      } catch (e: any) {
        setError(e?.message ?? "Unexpected error");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [page, size, sort, filters, selectedPerson]);

  const toggleSort = (key: keyof Person | "id" | "person" | "actions") => {
    if (key === "actions") return;
    setPage(0);
    let sortKey: SortState["sortBy"] = key;
    if (key === "person") sortKey = "firstName";
    if (key === "department") sortKey = "department.name";
    if (key === "title") sortKey = "title.name";
    setSort((s) =>
      s.sortBy === sortKey ? { sortBy: sortKey, direction: s.direction === "asc" ? "desc" : "asc" } : { sortBy: sortKey, direction: "asc" }
    );
  };

  const handlePersonClick = (person: Person) => {
    setSelectedPerson(person);
  };

  const activeFilters = useMemo(() => {
    const active: Array<{ key: keyof Filters; label: string }> = [];
    if (filters.q) active.push({ key: "q", label: `Search: "${filters.q}"` });
    if (filters.departmentId) {
      const d = departments.find((x) => String(x.id) === String(filters.departmentId));
      active.push({ key: "departmentId", label: `Department: ${d?.name ?? filters.departmentId}` });
    }
    if (filters.titleId) {
      const t = titles.find((x) => String(x.id) === String(filters.titleId));
      active.push({ key: "titleId", label: `Title: ${t?.name ?? filters.titleId}` });
    }
    if (filters.gender) active.push({ key: "gender", label: `Gender: ${filters.gender}` });
    return active;
  }, [filters, departments, titles]);

  const clearFilter = (key: keyof Filters) => {
    if (key === "q") setSearch("");
    setFilters((f) => ({ ...f, [key]: "" }));
    setPage(0);
  };

  const clearAllFilters = () => {
    setSearch("");
    setFilters(defaultFilters);
    setPage(0);
  };

  const hasActiveFilters = activeFilters.length > 0;

  if (selectedPerson) {
    return (
      <div className="people-container">
        <PersonProfileView
          person={selectedPerson}
          onBack={() => setSelectedPerson(null)}
        />
      </div>
    );
  }

  if (loading) return <div className="people-container">Loading...</div>;
  if (error) return <div className="people-container">Error: {error}</div>;

  const totalPages = Math.max(1, Math.ceil(total / size));
  const isPrevDisabled = page === 0;
  const isNextDisabled = page + 1 >= totalPages;

  return (
    <div className="people-container">
      <div className="people-header">
        <div className="search-wrapper">
          <SearchIcon className="search-icon" width={18} height={18} />
          <input
            className="people-search-input base-input"
            placeholder="Search by name, email, phone, address..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                setFilters((f) => ({ ...f, q: search }));
                setPage(0);
              }
            }}
          />
          {search && (
            <button 
              className="search-clear base-button"
              onClick={() => {
                setSearch("");
                setFilters((f) => ({ ...f, q: "" }));
                setPage(0);
              }}
              title="Clear search"
            >
              ×
            </button>
          )}
        </div>
        
        <div className="header-actions">
          <button
            className="filter-toggle base-button"
            onClick={() => setFiltersOpen((v) => !v)}
            title="Show/Hide Filters"
          >
            {filtersOpen ? <ChevronUpIcon width={16} height={16} /> : <ChevronDownIcon width={16} height={16} />}
            Filters
            {hasActiveFilters && <span className="filter-count">{activeFilters.length}</span>}
          </button>
          
          {hasActiveFilters && (
            <button
              className="clear-all-button base-button"
              onClick={clearAllFilters}
              title="Clear all filters"
            >
              Clear All
            </button>
          )}
        </div>
      </div>

      {filtersOpen && (
        <div className="filters-section">
          <div className="filter-controls">
            <div className="filter-group">
              <label className="filter-label">Department</label>
              <div className="custom-select">
                <select
                  className="filter-dropdown base-input"
                  value={filters.departmentId}
                  onChange={(e) => { setFilters({ ...filters, departmentId: e.target.value }); setPage(0); }}
                >
                  <option value="">All Departments</option>
                  {departments.map((d) => (
                    <option key={d.id} value={d.id}>{d.name}</option>
                  ))}
                </select>
              </div>
            </div>
            
            <div className="filter-group">
              <label className="filter-label">Title</label>
              <div className="custom-select">
                <select
                  className="filter-dropdown base-input"
                  value={filters.titleId}
                  onChange={(e) => { setFilters({ ...filters, titleId: e.target.value }); setPage(0); }}
                >
                  <option value="">All Titles</option>
                  {titles.map((t) => (
                    <option key={t.id} value={t.id}>{t.name}</option>
                  ))}
                </select>
              </div>
            </div>
            
            <div className="filter-group">
              <label className="filter-label">Gender</label>
              <div className="custom-select">
                <select
                  className="filter-dropdown base-input"
                  value={filters.gender}
                  onChange={(e) => { setFilters({ ...filters, gender: e.target.value }); setPage(0); }}
                >
                  <option value="">All Genders</option>
                  <option value="Female">Female</option>
                  <option value="Male">Male</option>
                  <option value="Other">Other</option>
                </select>
              </div>
            </div>
          </div>
          
          {hasActiveFilters && (
            <div className="active-filters">
              <span className="filters-label">Active filters:</span>
              <div className="filter-chips">
                {activeFilters.map((filter) => (
                  <span className="filter-chip" key={filter.key as string}>
                    <span className="filter-chip-text">{filter.label}</span>
                    <button className="filter-chip-remove base-button" onClick={() => clearFilter(filter.key)} aria-label="Remove">×</button>
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      <div className="table-wrapper">
        <table className="people-table">
          <thead>
            <tr>
              {columns.map((c) => (
                <th key={c.key as string} className={c.key === "actions" ? "" : "sortable"} onClick={() => toggleSort(c.key)}>
                  {c.label}
                  {c.key !== "actions" && (
                    <span className="sort-icon">{sort.sortBy === (c.key === "person" ? "firstName" : c.key === "department" ? "department.name" : c.key === "title" ? "title.name" : c.key) ? (sort.direction === "asc" ? "▲" : "▼") : "↕"}</span>
                  )}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((p) => (
              <tr key={p.id} className="table-row">
                <td className="person-cell clickable-cell" onClick={() => handlePersonClick(p)}>
                  <PersonAvatar person={p} />
                </td>
                <td className="clickable-cell" onClick={() => handlePersonClick(p)}>{p.email}</td>
                <td className="clickable-cell" onClick={() => handlePersonClick(p)}>{p.phoneNumber ?? "-"}</td>
                <td className="clickable-cell" onClick={() => handlePersonClick(p)}>{p.department?.name ?? "-"}</td>
                <td className="clickable-cell" onClick={() => handlePersonClick(p)}>{p.title?.name ?? "-"}</td>
                <td className="clickable-cell" onClick={() => handlePersonClick(p)}>{p.gender ?? "-"}</td>
                <td className="clickable-cell" onClick={() => handlePersonClick(p)}>{p.address ?? "-"}</td>
                <td className="actions-cell">
                  <button 
                    className="actions-button base-button"
                    onClick={(e) => {
                      e.stopPropagation();
  
                    }}
                    title="More actions"
                  >
                    •••
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="pagination-bar">
        <div className="page-info">
          Page {page + 1} of {Math.max(1, Math.ceil(total / size))} • Total {total}
        </div>
        <div className="pager">
          <button className={`filter-button base-button ${isPrevDisabled ? "is-disabled" : ""}`} onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={isPrevDisabled}>
            Previous
          </button>
          <button className={`filter-button base-button ${isNextDisabled ? "is-disabled" : ""}`} onClick={() => setPage((p) => p + 1)} disabled={isNextDisabled}>
            Next
          </button>
        </div>
      </div>
    </div>
  );
}

function PersonAvatar({ person }: { person: Person }) {
  const [imageError, setImageError] = useState(false);
  const hasValidImage = person.profilePictureUrl && !imageError;

  return (
    <div className="person-avatar">
      <div className="person-photo base-photo">
        {hasValidImage ? (
          <img
            src={person.profilePictureUrl as string}
            alt={`${person.firstName} ${person.lastName}`}
            onError={() => setImageError(true)}
          />
        ) : (
          <UserAvatarIcon width={20} height={20} />
        )}
      </div>
      <div className="person-name">
        <span className="person-full-name">{person.firstName} {person.lastName}</span>
      </div>
    </div>
  );
}