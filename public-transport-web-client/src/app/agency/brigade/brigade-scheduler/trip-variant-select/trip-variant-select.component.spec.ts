import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TripVariantSelectComponent } from './trip-variant-select.component';

describe('TripVariantSelectComponent', () => {
  let component: TripVariantSelectComponent;
  let fixture: ComponentFixture<TripVariantSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TripVariantSelectComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TripVariantSelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
