import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {provideRouter} from '@angular/router';
import {TranslocoTestingModule} from '@jsverse/transloco';

import {SidebarComponent} from './sidebar.component';
import {LoginService} from '../../auth/login.service';
import {AgencyStorageService} from '../../auth/agency-storage.service';

describe('SidebarComponent', () => {
    let component: SidebarComponent;
    let fixture: ComponentFixture<SidebarComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SidebarComponent,
                TranslocoTestingModule.forRoot({
                    langs: {pl: {'sidebar.collapse': 'Zwiń menu', 'sidebar.expand': 'Rozwiń menu'}},
                    translocoConfig: {availableLangs: ['pl'], defaultLang: 'pl'}
                })
            ],
            providers: [
                provideNoopAnimations(),
                provideRouter([]),
                {provide: AgencyStorageService, useValue: {getInstance: () => 'test'}}
            ]
        }).overrideComponent(SidebarComponent, {
            set: {
                providers: [
                    {provide: LoginService, useValue: {hasRoleSuperAdmin: () => false}}
                ]
            }
        }).compileComponents();

        fixture = TestBed.createComponent(SidebarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
