import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrandHeaderComponent } from './brand-header.component';

describe('BrandHeaderComponent', () => {
  let component: BrandHeaderComponent;
  let fixture: ComponentFixture<BrandHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrandHeaderComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BrandHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
