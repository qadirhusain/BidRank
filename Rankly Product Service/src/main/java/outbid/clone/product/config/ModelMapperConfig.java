package outbid.clone.product.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import outbid.clone.product.dto.ProductRequest;
import outbid.clone.product.entity.Product;

import static org.modelmapper.config.Configuration.AccessLevel.PRIVATE;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(PRIVATE)
                .setSkipNullEnabled(true);

        modelMapper.createTypeMap(ProductRequest.class, Product.class)
                        .addMappings(mapper -> {
                            mapper.skip(Product::setId);
                            mapper.skip(Product::setCanonicalUrl);
                            mapper.skip(Product::setStatus);
                            mapper.skip(Product::setCreatedAt);
                            mapper.skip(Product::setUpdatedAt);
                        });

        return modelMapper;
    }
}