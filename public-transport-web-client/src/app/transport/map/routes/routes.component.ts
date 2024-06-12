import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Route, Stop, StopDetails} from "../../../generated";

@Component({
    selector: 'app-routes',
    templateUrl: './routes.component.html',
    styleUrl: './routes.component.scss'
})
export class RoutesComponent {

    @Input() stop: Stop | null;
    @Input() stopDetails: StopDetails | null;
    @Input() routes: Route[] = [];

    @Output() clickLine = new EventEmitter<string>();

    constructor() {
    }

    onClickLine(line: string) {
        this.clickLine.emit(line);
    }

    public getTrams(): string[] {
        const routes = this.routes
            .filter(route => this.isTram(route))
            .map(route => route.shortName || '')
            .sort((prev, curr) => Number(prev) - Number(curr));
        return [...new Set(routes)];
    }

    public hasTrams(): boolean {
        return this.getTrams().length > 0;
    }

    public getBuses(): string[] {
        const routes = this.routes
            .filter(route => this.isBus(route))
            .filter(route => !this.isNightBus(route))
            .map(route => route.shortName || '')
            .sort((prev, curr) => prev.localeCompare(curr));
        return [...new Set(routes)];
    }

    public hasBuses(): boolean {
        return this.getBuses().length > 0;
    }

    public getNightBus(): string[] {
        const routes = this.routes
            .filter(route => this.isNightBus(route))
            .map(route => route.shortName || '')
            .sort((prev, curr) => prev.localeCompare(curr));
        return [...new Set(routes)];
    }

    public hasNightBuses(): boolean {
        return this.getNightBus().length > 0;
    }

    private isTram(route: Route): boolean {
        return route.mode === 'TRAM';
    }

    private isBus(route: Route): boolean {
        return route.mode === 'BUS';
    }

    private isNightBus(route: Route): boolean {
        return this.isBus(route) && (route.shortName || '').startsWith('N');
    }

    public isFast(line: string) {
        return line.startsWith('5') && line.length === 3;
    }

}
