export interface TripListTab {
    path: string;
    label: string;
}

export const TRIP_LIST_TABS: TripListTab[] = [
    {path: 'info', label: 'Informacje Podstawowe'},
    {path: 'variants', label: 'Trasy&Warianty'},
    {path: 'timetable', label: 'Rozkład Jazdy'},
];
