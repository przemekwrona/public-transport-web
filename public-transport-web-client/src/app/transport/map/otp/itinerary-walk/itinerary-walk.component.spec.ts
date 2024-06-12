import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ItineraryWalkComponent } from './itinerary-walk.component';

describe('ItineraryWalkComponent', () => {
  let component: ItineraryWalkComponent;
  let fixture: ComponentFixture<ItineraryWalkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItineraryWalkComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ItineraryWalkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
