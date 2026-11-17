import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {ActivatedRoute, Data, Router} from '@angular/router';
import {BehaviorSubject} from 'rxjs';

import {ProfileListComponent} from './profile-list.component';
import {BASE_PATH} from '../../../generated/public-transport-api';

describe('ProfileListComponent', () => {
    let fixture: ComponentFixture<ProfileListComponent>;
    let http: HttpTestingController;
    let routeData: BehaviorSubject<Data>;

    const agencies = {
        agencies: [
            {agencyCode: 'MPK', agencyName: 'MPK Kraków', routesNo: 3, createdAt: '2024-01-01T00:00:00Z'},
            {agencyCode: 'ZTM', agencyName: 'ZTM', routesNo: 1, createdAt: '2024-02-01T00:00:00Z'}
        ]
    };

    beforeEach(async () => {
        routeData = new BehaviorSubject<Data>({});
        await TestBed.configureTestingModule({
            imports: [ProfileListComponent],
            providers: [
                provideHttpClient(),
                provideHttpClientTesting(),
                {provide: BASE_PATH, useValue: '/api/v1'},
                {provide: ActivatedRoute, useValue: {data: routeData.asObservable()}},
                {provide: Router, useValue: {navigate: () => Promise.resolve(true)}}
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ProfileListComponent);
        http = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        http.verify();
    });

    it('should create', () => {
        fixture.detectChanges();
        expect(fixture.componentInstance).toBeTruthy();
    });

    it('shows the agency photo when one is set and the default avatar otherwise', () => {
        routeData.next({agencies});
        fixture.detectChanges();

        const withPhoto = http.expectOne('/api/v1/agency/MPK/photo');
        const withoutPhoto = http.expectOne('/api/v1/agency/ZTM/photo');
        withPhoto.flush(new Blob([Uint8Array.from([137, 80, 78, 71])], {type: 'image/png'}));
        withoutPhoto.flush(null, {status: 204, statusText: 'No Content'});
        fixture.detectChanges();

        const images: NodeListOf<HTMLImageElement> = fixture.nativeElement.querySelectorAll('tbody img');
        expect(images.length).toBe(2);
        expect(images[0].src).toContain('blob:');
        expect(images[1].src).toContain('blank.png');
    });
});
